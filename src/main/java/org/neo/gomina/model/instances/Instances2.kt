package org.neo.gomina.model.instances

class Identification(
        var env: String? = null ,
        var id: String? = null, // Unique by env
        var name: String? = null ,// X Replication
        var service: String? = null, // Z Partitioning
        var type: String? = null // Y Functional
)

class Project(
        var project: String? = null,
        var latestVersion: String? = null,
        var latestRevision: String? = null,

        var releasedVersion: String? = null,
        var releasedRevision: String? = null
)

class Deployment(
        var deployHost: String? = null,
        var deployFolder: String? = null,
        var deployVersion: String? = null,
        var deployRevision: String? = null
)

class Configuration(
        var confCommited: Boolean? = null,
        var confUpToDate: Boolean? = null
)

class Run(
        var pid: String? = null,
        var host: String? = null,
        var status: String? = null,
        var version: String? = null,
        var revision: String? = null
)

class Cluster(
        var cluster: Boolean = false,
        var participating: Boolean = false,
        var leader: Boolean = false
)

class CustomJava(
        var jmx: Int? = null
)

class CustomFrameworks(
        var busVersion: String? = null,
        var coreVersion: String? = null

)

class CustomRedis(
        var redisHost: String? = null,
        var redisPort: Int? = null,
        var redisMasterHost: String? = null,
        var redisMasterPort: Int? = null,
        var redisMasterLink: Boolean? = null,
        var redisMasterLinkDownSince: String? = null,
        var redisOffset: Int? = null,
        var redisOffsetDiff: Int? = null,
        var redisMaster: Boolean? = null,
        var redisRole: String? = null,
        var redisRW: String? = null,
        var redisMode: String? = null,
        var redisStatus: String? = null,
        var redisSlaveCount: Int? = null,
        var redisClientCount: Int? = null
)

class CustomFix(
        var quickfixPersistence: String? = null,
        var connected: Boolean?
)

class Instance2 (
        val identification: Identification,
        val deployment: Deployment,
        val configuration: Configuration,
        val run: Run,
        val project: Project,
        val cluster: Cluster,
        val customJava: CustomJava,
        val customFrameworks: CustomFrameworks,
        val customRedis: CustomRedis,
        val customFix: CustomFix,

        var unexpected: Boolean = false,
        var unexpectedHost: Boolean = false
)