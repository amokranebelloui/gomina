package org.neo.gomina.model.monitoring

import org.neo.gomina.model.version.Version
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
        val version: Version?,
        val cluster: ClusterInfo,

        var properties: Map<String, Any?> = mapOf()
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

data class ClusterInfo(
        var cluster: Boolean = false,
        var participating: Boolean = false,
        var leader: Boolean = false
)

