package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.model.component.*
import org.neo.gomina.model.issues.IssueProjects
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmBranch
import org.neo.gomina.model.version.Version
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

    @Inject lateinit var issues: IssueProjects

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 1)
        logger.info("Components Database connected $host $port")
    }

    override fun getAll(): List<Component> {
        pool.resource.use { jedis ->
            val componentKeys = jedis.keys("component:*")
            val branchKeys = jedis.keys("branch:*")
            val pipe = jedis.pipelined()
            val components = componentKeys.map { it.substring(10) to pipe.hgetAll(it) }
            val branchesMap = branchKeys
                    .map { key -> key.split(":").let { Triple(it[1], it[2], pipe.hgetAll(key)) } }
                    .groupBy({it.first}, {it.second to it.third})
            pipe.sync()
            return components.map { (id, data) ->
                val branches = branchesMap[id]
                        ?.map { (id, bData) -> toBranch(id, bData.get()) }
                        ?: emptyList()
                toComponent(id, data.get(), branches)
            }
        }
    }

    override fun get(componentId: String): Component? {
        pool.resource.use { jedis ->
            val branchKeys = jedis.keys("branch:$componentId:*")
            val pipe = jedis.pipelined()
            val component = pipe.hgetAll("component:$componentId")
            val branches = branchKeys.map { key ->
                key.split(":")[2] to pipe.hgetAll(key)
            }
            pipe.sync()
            return component?.let { toComponent(
                    componentId,
                    it.get(),
                    branches.map { (id, data) -> toBranch(id, data.get()) }
            )}
        }
    }

    override fun getCommitLog(componentId: String, branch: String): List<Commit> {
        pool.resource.use { jedis ->
            return jedis.pipelined().let { pipe ->
                val commits = jedis.zrevrange("branch_commits:$componentId:$branch", 0, 100).map { rev ->
                    val data = pipe.hgetAll("commit:$componentId:$rev")
                    val branches = pipe.smembers("commit_branches:$componentId:$rev")
                    data to branches
                }
                pipe.sync()
                commits.map { (data, branches) -> data.get() to branches.get() }
                        .map { (data, branches) -> data.toCommit(branches) }
            }
        }
    }

    private fun Map<String, String>.toCommit(branches: Collection<String>): Commit {
        return Commit(
                revision = this["revision"] ?: "",
                date = LocalDateTime.parse(this["date"] ?: "1970-01-01T00:00:00.000Z", ISO_DATE_TIME), // FIXME
                author = this["author"],
                message = this["message"],
                branches = branches.toList(),
                version = this["version"]
        )
    }

    private fun toComponent(id: String, map: Map<String, String>, branches: List<Branch>): Component {
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

                branches = branches,
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

    private fun toBranch(id: String, map: Map<String, String>): Branch {
        return Branch(
                name = id,
                buildServer = map["build_server"] ?: "",
                buildJob = map["build_job"],
                buildNumber = map["build_number"],
                buildStatus = map["build_status"],
                buildBuilding = map["build_building"]?.toBoolean(),
                buildTimestamp = map["build_timestamp"]?.toLong(),
                dismissed = map["dismissed"]?.toBoolean() == true
        )
    }

    override fun add(component: NewComponent) {
        pool.resource.use { jedis ->
            if (jedis.exists("component:${component.id}")) {
                throw Exception("${component.id} already exists")
            }
            jedis.persist("component:${component.id}", listOfNotNull(
                    "artifact_id" to component.artifactId,
                    "label" to component.label,
                    "type" to component.type,
                    "systems" to component.systems.toStr(),
                    "languages" to component.languages.toStr(),
                    "tags" to component.tags.toStr(),
                    "scm_type" to (component.scm?.type ?: ""),
                    "scm_url" to (component.scm?.url ?: ""),
                    "scm_path" to (component.scm?.path ?: ""),
                    "scm_username" to (component.scm?.username ?: ""),
                    "scm_password_alias" to (component.scm?.passwordAlias ?: ""),
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

    override fun editScm(componentId: String, type: String, url: String, path: String?, hasMetadata: Boolean, username: String?, passwordAlias: String?) {
        pool.resource.use { jedis ->
            jedis.persist("component:$componentId", mapOf(
                    "scm_type" to type,
                    "scm_url" to url,
                    "scm_path" to path,
                    "scm_username" to username,
                    "scm_password_alias" to passwordAlias,
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

    override fun updateBranchBuildStatus(componentId: String, branchId: String, number: String?, status: String?, building: Boolean?, timestamp: Long?) {
        pool.resource.use { jedis ->
            jedis.hmset("branch:$componentId:$branchId", listOfNotNull(
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

    override fun getVersions(componentId: String, branchId: String?): List<VersionRelease> {
        pool.resource.use { jedis ->
            val suffix = branchId?.let { ":$it" } ?: ":*"
            val keys = jedis.keys("versions:$componentId$suffix")
            return keys.flatMap { key ->
                val branch = key.substring(key.lastIndexOf(":") + 1)
                jedis.zrevrangeWithScores(key, 0, -1).mapNotNull {
                    val artifact = Artifact.parse(it.element)
                    val version = artifact?.getVersion()
                    val releaseDate = Instant.ofEpochMilli(it.score.toLong()).atZone(ZoneOffset.UTC).toLocalDateTime()
                    if (artifact != null && version != null) VersionRelease(artifact, version, releaseDate, branch) else null
                }
            }
            .sortedWith(compareBy({ it.releaseDate }, {it.version}))
            .reversed()
        }
    }

    override fun addVersions(componentId: String, branch: String, versions: List<VersionRelease>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                versions.forEach { v ->
                    val time = v.releaseDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli().toDouble()
                    pipe.zadd("versions:$componentId:$branch", time, v.artifact.toString())
                    pipe.zadd("versions:$componentId", time, v.artifact.toString())
                }
                pipe.sync()
            }
        }
    }

    override fun cleanSnapshotVersions(componentId: String) {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            jedis.keys("versions:$componentId:*-SNAPSHOT").forEach {
                pipe.del(it)
            }
            pipe.sync()
        }
    }

    override fun updateBranches(componentId: String, branches: List<ScmBranch>) {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            //pipe.hset("component:$componentId", "branches", branches.map { it.name }.toStr())
            branches.forEach {
                pipe.persist("branch:$componentId:${it.name}", mapOf(
                        "update_time" to now(Clock.systemUTC()).format(ISO_DATE_TIME)
                ))
            }
            pipe.sync()
        }
    }

    override fun editBranchBuild(componentId: String, branchId: String, buildServer: String, buildJob: String) {
        pool.resource.use { jedis ->
            jedis.persist("branch:$componentId:$branchId", mapOf(
                    "build_server" to buildServer,
                    "build_job" to buildJob
            ))
        }
    }

    override fun dismissBranch(componentId: String, branchId: String) {
        pool.resource.use { jedis ->
            jedis.hset("branch:$componentId:$branchId", "dismissed", true.toString())
        }
    }

    override fun reactivateBranch(componentId: String, branchId: String) {
        pool.resource.use { jedis ->
            jedis.hset("branch:$componentId:$branchId", "dismissed", false.toString())
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
                    commit.branches.forEach {
                        pipe.zadd("branch_commits:$componentId:$it", time, commit.revision)
                        commit.branches.forEach { branch ->
                            pipe.sadd("commit_branches:$componentId:${commit.revision}", branch)
                        } 
                    }
                    pipe.hmset("commit:$componentId:${commit.revision}", listOfNotNull(
                            "revision" to commit.revision,
                            commit.date.let { "date" to it.format(ISO_DATE_TIME) },
                            commit.author?.let { "author" to it },
                            commit.message?.let { "message" to it },
                            commit.version?.let { "version" to it }
                    ).toMap())
                    commit.issues(issues).map {
                        pipe.sadd("issue_components:$it", componentId)
                    }
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

    override fun componentsForIssue(issue: String): Set<String> {
        pool.resource.use { jedis ->
            return jedis.smembers("issue_components:$issue")
        }
    }
    
}