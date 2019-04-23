package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.NewComponent
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.scm.Branch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.version.Version
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

class ComponentRepoFile : ComponentRepo, AbstractFileRepo() {
    companion object {
        private val logger = LogManager.getLogger(ComponentRepoFile.javaClass)
    }

    @Inject @Named("components.file")
    private lateinit var file: File

    fun read(file: File): List<Component> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Component> = read(file)
    override fun get(componentId: String): Component? = read(file).find { it.id == componentId }
    override fun getCommitLog(componentId: String): List<Commit> { TODO("not implemented") }

    override fun add(component: NewComponent) { TODO("not implemented") }

    override fun editLabel(componentId: String, label: String) { TODO("not implemented") }
    override fun editType(componentId: String, type: String) { TODO("not implemented") }
    override fun editOwner(componentId: String, owner: String?) { TODO("not implemented") }
    override fun editCriticality(componentId: String, critical: Int?) { TODO("not implemented") }
    override fun editArtifactId(componentId: String, artifactId: String?) { TODO("not implemented") }
    override fun editScm(componentId: String, type: String, url: String, path: String?) { TODO("not implemented") }
    override fun editSonar(componentId: String, server: String?) { TODO("not implemented") }
    override fun editBuild(componentId: String, server: String?, job: String?) { TODO("not implemented") }

    override fun addSystem(componentId: String, system: String) { TODO("not implemented") }
    override fun deleteSystem(componentId: String, system: String) { TODO("not implemented") }

    override fun addLanguage(componentId: String, language: String) { TODO("not implemented")}
    override fun deleteLanguage(componentId: String, language: String) { TODO("not implemented")}
    override fun addTag(componentId: String, tag: String) { TODO("not implemented")}
    override fun deleteTag(componentId: String, tag: String) { TODO("not implemented")}


    override fun disable(componentId: String) { TODO("not implemented") }
    override fun enable(componentId: String) { TODO("not implemented") }

    override fun updateCodeMetrics(componentId: String, loc: Double?, coverage: Double?) { TODO("not implemented") }
    override fun updateBuildStatus(componentId: String, number: String?, status: String?, building: Boolean?, timestamp: Long?) { TODO("not implemented") }

    override fun updateVersions(componentId: String, latest: Version?, released: Version?, changes: Int?) { TODO("not implemented") }
    override fun updateBranches(componentId: String, branches: List<Branch>) { TODO("not implemented") }
    override fun updateDocFiles(componentId: String, branches: List<String>) { TODO("not implemented") }
    override fun updateCommitLog(componentId: String, commite: List<Commit>) { TODO("not implemented") }
    override fun updateCommitToRelease(componentId: String, commitToRelease: Int?) { TODO("not implemented") }
}

class RedisComponentRepo : ComponentRepo {
    companion object {
        private val logger = LogManager.getLogger(RedisComponentRepo.javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 1)
        logger.info("Components Database connected $host $port")
    }

    override fun getAll(): List<Component> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            val data = jedis.keys("component:*").map { it.substring(10) to pipe.hgetAll(it) }
            pipe.sync()
            return data.map { (id, data) ->
                val commits = getCommits(jedis, id)
                toComponent(id, data.get(), commits)
            }
        }
    }

    override fun get(componentId: String): Component? {
        pool.resource.use { jedis ->
            val commits = getCommits(jedis, componentId)
            return jedis.hgetAll("component:$componentId")
                    ?.let { toComponent(componentId, it, commits) }
        }
    }

    override fun getCommitLog(componentId: String): List<Commit> {
        pool.resource.use { jedis ->
            return getCommits(jedis, componentId)
        }
    }

    private fun getCommits(jedis: Jedis, componentId: String): List<Commit> {
        return jedis.pipelined().let { pipe ->
            //jsonMapper.readValue<Commit>(it)
            val commits = jedis.zrevrange("commits:$componentId", 0, 100).map { rev ->
                pipe.hgetAll("commit:$componentId:$rev")
            }
            pipe.sync()
            commits.map { it.get() }.map {
                Commit(
                        revision = it["revision"] ?: "",
                        date = it["date"]?.let { LocalDateTime.parse(it, ISO_DATE_TIME) },
                        author = it["author"],
                        message = it["message"],
                        release = it["release"],
                        newVersion = it["newVersion"],
                        issues = it["issues"].toList())
            }
        }
    }

    private fun toComponent(id: String, map: Map<String, String>, commits: List<Commit>): Component {
        return Component(
                id = id,
                label = map["label"],
                type = map["type"],
                systems = map["systems"].toList(),
                languages = map["languages"].toList(),
                tags = map["tags"].toList(),
                scm = Scm(
                        type = map["scm_type"] ?: "",
                        url = map["scm_url"] ?: "",
                        path = map["scm_path"] ?: "",
                        username = map["scm_username"] ?: "",
                        passwordAlias = map["scm_password_alias"] ?: ""
                ) ,
                owner = map["owner"],
                critical = map["critical"]?.toInt(),
                maven = map["maven"],
                sonarServer = map["sonar_server"] ?: "",
                jenkinsServer = map["jenkins_server"] ?: "",
                jenkinsJob = map["jenkins_job"],

                latest = map["latest_version"]?.let { Version(it, map["latest_revision"]) },
                released = map["released_version"]?.let { Version(it, map["latest_revision"]) },
                changes = map["changes"]?.toInt(),

                branches = map["branches"].toList().map { Branch(it) },
                docFiles = map["doc_files"].toList(),
                commitToRelease = map["commit_to_release"]?.toInt(),
                commitLog = commits,

                loc = map["loc"]?.toDouble(),
                coverage = map["coverage"]?.toDouble(),
                buildNumber = map["build_number"],
                buildStatus = map["build_status"],
                buildBuilding = map["build_building"]?.toBoolean(),
                buildTimestamp = map["build_timestamp"]?.toLong(),
                disabled = map["disabled"]?.toBoolean() == true
        )
    }

    override fun add(component: NewComponent) {
        pool.resource.use { jedis ->
            if (jedis.exists("component:${component.id}")) {
                throw Exception("${component.id} already exists")
            }
            jedis.hmset("component:${component.id}", listOfNotNull(
                    "label" to component.label,
                    "maven" to component.artifactId,
                    "type" to component.type,
                    "systems" to component.systems.toStr(),
                    "languages" to component.languages.toStr(),
                    "tags" to component.tags.toStr(),
                    "scm_type" to (component.scm?.type ?: ""),
                    "scm_url" to (component.scm?.url ?: ""),
                    "scm_path" to (component.scm?.path ?: ""),
                    component.sonarServer?.let { "sonar_server" to it },
                    component.jenkinsServer?.let { "jenkins_server" to it },
                    component.jenkinsJob?. let { "jenkins_job" to it }
            ).toMap())
        }
    }

    override fun editLabel(componentId: String, label: String) {
        if (label.isBlank()) {
            throw Exception("$componentId label cannot be blank")
        }
        pool.resource.use { jedis ->
            jedis.hset("component:$componentId", "label", label)
        }
    }

    override fun editType(componentId: String, type: String) {
        pool.resource.use { jedis ->
            jedis.hset("component:$componentId", "type", type)
        }
    }

    override fun editOwner(componentId: String, owner: String?) {
        owner?.let {
            pool.resource.use { jedis ->
                jedis.hset("component:$componentId", "owner", owner)
            }
        } 
    }

    override fun editCriticality(componentId: String, critical: Int?) {
        critical?.let {
            pool.resource.use { jedis ->
                jedis.hset("component:$componentId", "critical", critical.toString())
            }
        }
    }

    override fun editArtifactId(componentId: String, artifactId: String?) {
        artifactId?.let {
            pool.resource.use { jedis ->
                jedis.hset("component:$componentId", "maven", artifactId)
            }
        }
    }

    override fun editScm(componentId: String, type: String, url: String, path: String?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    "scm_type" to type,
                    "scm_url" to url,
                    path?. let { "scm_path" to it }
            ).toMap())
        }
    }

    override fun editSonar(componentId: String, server: String?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    server?. let { "sonar_server" to it }
            ).toMap())
        }
    }

    override fun editBuild(componentId: String, server: String?, job: String?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    server?. let { "jenkins_server" to it },
                    job?. let { "jenkins_job" to it }
            ).toMap())
        }
    }

    override fun addSystem(componentId: String, system: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "systems").toList().toSet()
                    .plus(system)
                    .toStr().let { jedis.hset("component:$componentId", "systems", it) }
        }
    }

    override fun deleteSystem(componentId: String, system: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "systems").toList().toSet()
                    .minus(system)
                    .toStr().let { jedis.hset("component:$componentId", "systems", it) }
        }
    }

    override fun addLanguage(componentId: String, language: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "languages").toList().toSet()
                    .plus(language)
                    .toStr().let { jedis.hset("component:$componentId", "languages", it) }
        }
    }

    override fun deleteLanguage(componentId: String, language: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "languages").toList().toSet()
                    .minus(language)
                    .toStr().let { jedis.hset("component:$componentId", "languages", it) }
        }
    }

    override fun addTag(componentId: String, tag: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "tags").toList().toSet()
                    .plus(tag)
                    .toStr().let { jedis.hset("component:$componentId", "tags", it) }
        }
    }

    override fun deleteTag(componentId: String, tag: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "tags").toList().toSet()
                    .minus(tag)
                    .toStr().let { jedis.hset("component:$componentId", "tags", it) }
        }
    }

    override fun disable(componentId: String) {
        pool.resource.use { jedis ->
            "component:$componentId".let { key ->
                if (jedis.exists(key)) jedis.hset(key, "disabled", "true")
            }
        }
    }

    override fun enable(componentId: String) {
        pool.resource.use { jedis ->
            "component:$componentId".let { key ->
                if (jedis.exists(key)) jedis.hset(key, "disabled", "false")
            }
        }
    }

    override fun updateCodeMetrics(componentId: String, loc: Double?, coverage: Double?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    "code_metrics_update_time" to now(Clock.systemUTC()).format(ISO_DATE_TIME),
                    loc?. let { "loc" to it.toString() },
                    coverage?. let { "coverage" to it.toString() }
            ).toMap())
        }
    }

    override fun updateBuildStatus(componentId: String, number: String?, status: String?, building: Boolean?, timestamp: Long?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    "build_update_time" to now(Clock.systemUTC()).format(ISO_DATE_TIME),
                    number?. let { "build_number" to it },
                    status?. let { "build_status" to it },
                    building?. let { "build_building" to it.toString() },
                    timestamp?. let { "build_timestamp" to it.toString() }
            ).toMap())
        }
    }

    override fun updateVersions(componentId: String, latest: Version?, released: Version?, changes: Int?) {
        pool.resource.use { jedis ->
            jedis.hmset("component:$componentId", listOfNotNull(
                    "scm_update_time" to now(Clock.systemUTC()).format(ISO_DATE_TIME),
                    latest?.version?.let { "latest_version" to it },
                    latest?.revision?.let { "latest_revision" to it },
                    released?.version?.let { "released_version" to it },
                    released?.revision?.let { "released_revision" to it },
                    changes?. let { "changes" to it.toString() }
            ).toMap())
        }
    }

    override fun updateBranches(componentId: String, branches: List<Branch>) {
        pool.resource.use { jedis ->
            jedis.hset("component:$componentId", "branches", branches.map { it.name }.toStr())
        }
    }

    override fun updateDocFiles(componentId: String, docFiles: List<String>) {
        pool.resource.use { jedis ->
            jedis.hset("component:$componentId", "doc_files", docFiles.toStr())
        }
    }

    override fun updateCommitLog(componentId: String, commits: List<Commit>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                commits.forEach { commit ->
                    val time = commit.date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()?.toDouble()
                    //val time = commit.date?.time?.toDouble()
                    pipe.zadd("commits:$componentId", time ?: 0.0, commit.revision)
                    pipe.hmset("commit:$componentId:${commit.revision}", listOfNotNull(
                            "revision" to commit.revision,
                            commit.date?.let { "date" to it.format(ISO_DATE_TIME) },
                            commit.author?.let { "author" to it },
                            commit.message?.let { "message" to it },
                            commit.release?.let { "release" to it },
                            commit.newVersion?.let { "newVersion" to it },
                            commit.issues.let { "issues" to it.toStr() }
                    ).toMap())
                }
            }
        }
    }

    override fun updateCommitToRelease(componentId: String, commitToRelease: Int?) {
        pool.resource.use { jedis ->
            if (commitToRelease != null) {
                jedis.hset("component:$componentId", "commit_to_release", commitToRelease.toString())
            }
            else {
                jedis.hdel("component:$componentId", "commit_to_release")
            }
        }
    }
}