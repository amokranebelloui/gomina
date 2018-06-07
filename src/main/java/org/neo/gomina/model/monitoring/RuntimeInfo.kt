package org.neo.gomina.model.monitoring

import org.joda.time.DateTimeZone

data class RuntimeInfo(
        val instanceId: String,
        val service: String?,
        val type: String?,
        private var lastTime:org.joda.time.LocalDateTime = org.joda.time.LocalDateTime(DateTimeZone.UTC),
        private var delayed:Boolean = false,

        val process: ProcessInfo,
        val jvm: JvmInfo,
        val cluster: ClusterInfo,
        val fix: FixInfo,
        val redis: RedisInfo,
        val version: VersionInfo,
        val dependencies: DependenciesInfo
) {
    fun touch() {
        lastTime = org.joda.time.LocalDateTime(DateTimeZone.UTC)
        delayed = false
    }
    fun checkDelayed(timeoutSeconds: Int = 5, notification: () -> Unit) {
        if (this.isLastTimeTooOld(timeoutSeconds) && !delayed) {
            delayed = true
            notification()
        }

    }
    private fun isLastTimeTooOld(timeoutSeconds: Int): Boolean {
        return org.joda.time.LocalDateTime(DateTimeZone.UTC).isAfter(lastTime.plusSeconds(timeoutSeconds))
    }
}

data class ProcessInfo(
        var pid: String? = null,
        var host: String? = null,
        var status: String? = null
)

data class JvmInfo(
        var jmx: Int? = null
)

data class ClusterInfo(
        var cluster: Boolean = false,
        var participating: Boolean = false,
        var leader: Boolean = false
)

data class FixInfo(
        var quickfixPersistence: String? = null
)

data class RedisInfo(
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

data class VersionInfo(
        var version: String? = null,
        var revision: String? = null
)

data class DependenciesInfo(
        var busVersion: String? = null,
        var coreVersion: String? = null
)

