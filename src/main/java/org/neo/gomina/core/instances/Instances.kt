package org.neo.gomina.core.instances

import java.util.*

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
        var status: String? = null,

        var project: String? = null,
        var deployHost: String? = null,
        var deployFolder: String? = null,
        var deployVersion: String? = null,
        var deployRevision: String? = null,
        var confCommited: Boolean? = null,
        var confUpToDate: Boolean? = null,
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

class InstanceRealTime (
        var env: String? = null ,
        var id: String? = null, // Unique by env
        var name: String? = null ,// X Replication
        var participating: Boolean = false,
        var leader: Boolean = false,
        var status: String? = null
)

typealias InstanceListener = (instance: InstanceRealTime) -> Unit

class Instances {
    val list = ArrayList<Instance>()
    private val index = HashMap<String, Instance>()

    fun get(id: String) = index[id]

    fun ensure(id: String, envId: String, type: String?, service: String?, instanceId: String, expected: Boolean = true): Instance {
        var instance = index[id]
        if (instance == null) {
            instance = Instance(
                    id = id,
                    env = envId,
                    type = type,
                    service = service,
                    name = instanceId,
                    unexpected = !expected
            )
            index.put(id, instance)
            list.add(instance)
        }
        return instance
    }
}