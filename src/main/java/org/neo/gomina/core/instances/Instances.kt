package org.neo.gomina.core.instances

data class ServiceDetail (
    val svc: String,
    val type: String? = null,
    val project: String? = null
)

class Instance (

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

class InstanceRealTime (
        var env: String? = null ,
        var id: String? = null, // Unique by env
        var name: String? = null ,// X Replication
        var participating: Boolean = false,
        var leader: Boolean = false,
        var status: String? = null
)

typealias InstanceListener = (instance: InstanceRealTime) -> Unit

interface InstanceDetailRepository {
    fun getInstances(): Collection<Instance>
    fun getInstances(envId: String): Collection<Instance>
    fun addInstance(id: String, instance: Instance)
    fun getInstance(id: String): Instance?
    fun getOrCreateInstance(id: String, unexpected: Instance): Instance
}

class InstanceDetailRepositoryImpl : InstanceDetailRepository {

    private val list = mutableListOf<Instance>()
    private val index = mutableMapOf<String, Instance>()

    override fun getInstances(): Collection<Instance> {
        return list
    }

    override fun getInstances(envId: String): Collection<Instance> {
        return list.filter { it.env == envId }
    }

    override fun addInstance(id: String, instance: Instance) {
        if (index.put(id, instance) == null) {
            list.add(instance)
        }
    }

    override fun getInstance(id: String): Instance? {
        return index[id]
    }

    override fun getOrCreateInstance(id: String, unexpected: Instance): Instance {
        var ins = index[id]
        if (ins == null) {
            ins = unexpected;
            addInstance(id, unexpected)
        }
        return ins
    }
}