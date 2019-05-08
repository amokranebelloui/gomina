package org.neo.gomina.api.common

import org.neo.gomina.model.user.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

data class UserRef(
        val id: String? = null,
        val shortName: String
)

fun User.toRef() = UserRef(id = id, shortName = shortName)

val LocalDateTime.toDateUtc: Date
    get() = this.let { Date.from(this.atZone(ZoneOffset.UTC).toInstant()) }

val LocalDate.toDateUtc: Date
    get() = this.let { Date.from(this.atStartOfDay(ZoneOffset.UTC).toInstant()) }

val LocalDate.toString: String
    get() = this.format(DateTimeFormatter.ISO_DATE)


fun Date?.toLocalDateTime(): LocalDateTime? {
    return this?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDateTime()
}

fun Date?.toLocalDate(): LocalDate? {
    return this?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate()
}

fun String?.toLocalDate(): LocalDate? {
    return LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
}

fun String?.splitParams() = this?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
