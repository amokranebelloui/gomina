package org.neo.gomina.api.instances

data class ServiceDetail (
    val svc: String,
    val type: String? = null,
    val project: String? = null
)

class InstanceDetail(

        var env: String? = null ,
        var id: String? = null, // Unique by env
        var name: String? = null ,// X Replication
        var service: String? = null, // Z Partitioning
        var type: String? = null, // Y Functional

        var unexpected: Boolean = false,
        var unexpectedHost: Boolean = false,

        var cluster: Boolean = false,
        var participating: Boolean = false,
        var leader: Boolean = false,

        var pid: String? = null,
        var host: String? = null,
        var status: String? = "NOINFO",

        var project: String? = null,
        var deployHost: String? = null,
        var deployFolder: String? = null,
        var deployVersion: String? = null,
        var deployRevision: String? = null,
        var confCommited: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null,
        var version: String? = null,
        var revision: String? = null,

        var latestVersion: String? = null,
        var latestRevision: String? = null,

        var releasedVersion: String? = null,
        var releasedRevision: String? = null,

        var jmx: Int? = null,
        var busVersion: String? = null,
        var coreVersion: String? = null,
        var quickfixPersistence: String? = null,
        var redisHost: String? = null,
        var redisPort: Int? = null,
        var redisMasterHost: String? = null,
        var redisMasterPort: Int? = null,
        var redisMasterLink: Boolean? = null,
        var redisMasterLinkDownSince: String? = null,
        var redisOffset: Long? = null,
        var redisOffsetDiff: Long? = null,
        var redisMaster: Boolean? = null,
        var redisRole: String? = null,
        var redisRW: String? = null,
        var redisMode: String? = null,
        var redisStatus: String? = null,
        var redisSlaveCount: Int? = null,
        var redisClientCount: Int? = null
)

data class InstanceRealTime (
        var env: String? = null ,
        var id: String? = null, // Unique by env
        var name: String? = null ,// X Replication
        var participating: Boolean = false,
        var leader: Boolean = false,
        var status: String? = null
)

typealias InstanceListener = (instance: InstanceRealTime) -> Unit

