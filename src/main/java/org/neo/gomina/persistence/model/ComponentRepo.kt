package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.component.*
import org.neo.gomina.model.scm.Branch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.version.Version
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.time.*
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

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
                        date = LocalDateTime.parse(it["date"] ?: "1970-01-01T00:00:00.000Z", ISO_DATE_TIME), // FIXME
                        author = it["author"],
                        message = it["message"],
                        release = it["release"],
                        newVersion = it["newVersion"])
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
                ),
                hasMetadata = map["has_metadata"]?.toBoolean() ?: false,
                inceptionDate = map["inception_date"]?.let { LocalDate.parse(it, ISO_DATE) },
                owner = map["owner"],
                criticity = map["criticity"]?.toInt(),
                artifactId = map["artifact_id"],
                sonarServer = map["sonar_server"] ?: "",
                jenkinsServer = map["jenkins_server"] ?: "",
                jenkinsJob = map["jenkins_job"],

                latest = map["latest_version"]?.let { Version(it, map["latest_revision"]) },
                released = map["released_version"]?.let { Version(it, map["latest_revision"]) },
                changes = map["changes"]?.toInt(),

                branches = map["branches"].toList().map { Branch(it) },
                docFiles = map["doc_files"].toList(),
                lastCommit = map["last_commit"]?.let { LocalDateTime.parse(it, ISO_DATE_TIME) },
                commitActivity = map["commit_activity"]?.toInt() ?: 0,
                commitToRelease = map["commit_to_release"]?.toInt(),

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
                    "artifact_id" to component.artifactId,
                    "label" to component.label,
                    "type" to component.type,
                    "systems" to component.systems.toStr(),
                    "languages" to component.languages.toStr(),
                    "tags" to component.tags.toStr(),
                    "scm_type" to (component.scm?.type ?: ""),
                    "scm_url" to (component.scm?.url ?: ""),
                    "scm_path" to (component.scm?.path ?: ""),
                    "has_metadata" to (component.hasMetadata.toString()),
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

    override fun editInceptionDate(componentId: String, inceptionDate: LocalDate?) {
        pool.resource.use { jedis ->
            jedis.persist("component:$componentId", mapOf("inception_date" to inceptionDate?.format(ISO_DATE)))
        }
    }

    override fun editOwner(componentId: String, owner: String?) {
        owner?.let {
            pool.resource.use { jedis ->
                jedis.hset("component:$componentId", "owner", owner)
            }
        } 
    }

    override fun editCriticity(componentId: String, criticity: Int?) {
        criticity?.let {
            pool.resource.use { jedis ->
                jedis.hset("component:$componentId", "criticity", criticity.toString())
            }
        }
    }

    override fun editArtifactId(componentId: String, artifactId: String?) {
        pool.resource.use { jedis ->
            jedis.persist("component:$componentId", mapOf("artifact_id" to artifactId))
        }
    }

    override fun editScm(componentId: String, type: String, url: String, path: String?, hasMetadata: Boolean) {
        pool.resource.use { jedis ->
            jedis.persist("component:$componentId", mapOf(
                    "scm_type" to type,
                    "scm_url" to url,
                    "scm_path" to path,
                    "has_metadata" to hasMetadata.toString()
            ))
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

    override fun getVersions(componentId: String): List<VersionRelease> {
        pool.resource.use { jedis ->
            return jedis.zrevrangeWithScores("versions:$componentId", 0, -1).mapNotNull {
                val artifactIdWithVersion = ArtifactId.tryWithVersion(it.element)
                val artifactId = artifactIdWithVersion?.toStrWithoutVersion()
                val version = artifactIdWithVersion?.getVersion()
                val releaseDate = Instant.ofEpochMilli(it.score.toLong()).atZone(ZoneOffset.UTC).toLocalDateTime()
                if (artifactId != null && version != null) VersionRelease(artifactId, version, releaseDate) else null
            }
        }
    }

    override fun updateVersions(componentId: String, latest: Version?, released: Version?, changes: Int?) {
        pool.resource.use { jedis ->
            val data = mapOf(
                    "scm_update_time" to now(Clock.systemUTC()).format(ISO_DATE_TIME),
                    "latest_version" to latest?.version,
                    "latest_revision" to latest?.revision,
                    "released_version" to released?.version,
                    "released_revision" to released?.revision,
                    "changes" to changes?.toString()
            )
            jedis.persist("component:$componentId", data)
        }
    }

    override fun updateVersions(componentId: String, versions: List<VersionRelease>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                versions.forEach { v ->
                    val artifactId = ArtifactId.tryWithGroup(v.artifactId)
                    val versionedArtifactId = ArtifactId(artifactId?.groupId, artifactId?.artifactId ?: "", v.version.version)
                    val time = v.releaseDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli().toDouble()
                    pipe.zadd("versions:$componentId", time, versionedArtifactId.toStr())
                }
                pipe.sync()
            }
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
                    val time = commit.date.atZone(ZoneOffset.UTC).toInstant().toEpochMilli().toDouble()
                    //val time = commit.date?.time?.toDouble()
                    pipe.zadd("commits:$componentId", time, commit.revision)
                    pipe.hmset("commit:$componentId:${commit.revision}", listOfNotNull(
                            "revision" to commit.revision,
                            commit.date.let { "date" to it.format(ISO_DATE_TIME) },
                            commit.author?.let { "author" to it },
                            commit.message?.let { "message" to it },
                            commit.release?.let { "release" to it },
                            commit.newVersion?.let { "newVersion" to it }
                    ).toMap())
                }
                pipe.sync()
            }
        }
    }

    override fun updateLastCommit(componentId: String, lastCommit: LocalDateTime?) {
        pool.resource.use { jedis ->
            if (lastCommit != null) {
                jedis.hset("component:$componentId", "last_commit", lastCommit.format(ISO_DATE_TIME))
            }
        }
    }

    override fun updateCommitActivity(componentId: String, activity: Int) {
        pool.resource.use { jedis ->
            if (activity != null) {
                jedis.hset("component:$componentId", "commit_activity", activity.toString())
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