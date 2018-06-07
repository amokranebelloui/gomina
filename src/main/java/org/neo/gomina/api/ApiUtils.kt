package org.neo.gomina.api

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

val LocalDateTime.toDateUtc: Date
    get() = this?.let { Date.from(this.atZone(ZoneOffset.UTC).toInstant()) }
