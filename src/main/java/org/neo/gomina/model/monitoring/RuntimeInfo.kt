package org.neo.gomina.model.monitoring

import java.time.Clock
import java.time.LocalDateTime

object ServerStatus {
    val LIVE = "LIVE"
    val LOADING = "LOADING"
    val DOWN = "DOWN"
    val OFFLINE = "OFFLINE"
    val DARK = "DARK"
}

data class RuntimeInfo(
        val instanceId: String,
        val service: String?,
        val type: String?,
        val lastTime:LocalDateTime,
        var delayed:Boolean = false,

        var sidecarStatus: String? = null,
        var sidecarVersion: String? = null,

        val process: ProcessInfo,
        val jvm: JvmInfo,
        val cluster: ClusterInfo,
        val fix: FixInfo,
        val redis: RedisInfo,
        val version: VersionInfo,
        val dependencies: DependenciesInfo
) {
    fun checkDelayed(timeoutSeconds: Int = 5, notification: () -> Unit) {
        if (this.isLastTimeTooOld(timeoutSeconds) && !delayed) {
            delayed = true
            notification()
        }

    }
    private fun isLastTimeTooOld(timeoutSeconds: Int): Boolean {
        return LocalDateTime.now(Clock.systemUTC()).isAfter(lastTime.plusSeconds(timeoutSeconds.toLong()))
    }
}

data class ProcessInfo(
        var pid: String? = null,
        var host: String? = null,
        var status: String? = null,
        var startTime: LocalDateTime? = null,
        var startDuration: Long? = null
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

