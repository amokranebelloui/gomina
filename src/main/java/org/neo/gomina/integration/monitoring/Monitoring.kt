package org.neo.gomina.integration.monitoring

import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

// FIXME Injectable
val TIMEOUT_SECONDS = 5

class Indicators(val instanceId: String) : ConcurrentHashMap<String, String>() {
    private var lastTime = LocalDateTime(DateTimeZone.UTC)
    private var delayed = false
    fun touch() {
        lastTime = LocalDateTime(DateTimeZone.UTC)
        delayed = false
    }
    fun checkDelayed(notification: () -> Unit) {
        if (this.isLastTimeTooOld() && !delayed) {
            delayed = true
            notification()
        }

    }
    private fun isLastTimeTooOld(): Boolean {
        return LocalDateTime(DateTimeZone.UTC).isAfter(lastTime.plusSeconds(TIMEOUT_SECONDS))
    }
}

class EnvMonitoring {
    val instances: MutableMap<String, Indicators> = ConcurrentHashMap()
    fun getForInstance(name: String): Indicators {
        return instances.getOrPut(name) { Indicators(name) }
    }
}