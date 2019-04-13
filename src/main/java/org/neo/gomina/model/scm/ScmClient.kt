package org.neo.gomina.model.scm

import org.neo.gomina.model.version.Version
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

data class Commit (
    val revision: String = "",
    val date: Date? = null,
    val author: String? = null,
    val message: String? = null,

    var release: String? = null, // new version: if the commit is a prepare release version change
    var newVersion: String? = null, // version: if the commit is a release
    var issues: List<String> = emptyList() // issue tracking system id
) {
    fun match(version: Version): Boolean {
        return this.release == version.version && this.revision == version.revision ||
                this.release?.let { Version.isStable(it) && this.release == version.version } == true
    }
}

private fun LocalDateTime.score(reference: LocalDateTime): Int {
    val sixMonthAgo = reference.minusMonths(6)
    val aMonthAgo = reference.minusMonths(1)
    val aWeekAgo = reference.minusWeeks(1)
    val aDayAgo = reference.minusDays(1)

    return when {
        this.isAfter(aDayAgo) -> 7
        this.isAfter(aWeekAgo) -> 5
        this.isAfter(aMonthAgo) -> 3
        this.isAfter(sixMonthAgo) -> 1
        else -> 0
    }
}

fun List<Commit>.activity(reference: LocalDateTime): Int {
    return this.mapNotNull { it.date }
            .mapNotNull { LocalDateTime.from(it.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()) }
            .filter { it.isAfter(reference.minusMonths(6)) }
            .map { it.score(reference) }
            .sumBy { it }
}

data class Branch(
        var name: String,
        var origin: String? = null,
        var originRevision: String? = null
)

interface ScmClient {

    fun getTrunk(): String = ""

    fun getBranches(): List<Branch> = arrayListOf()

    /** Get log from HEAD to revision, max @count elements */
    fun getLog(branch: String, rev: String, count: Int): List<Commit>

    /** get file for a revision, HEAD is -1 **/
    fun getFile(url: String, rev: String): String?

    fun listFiles(url: String, rev: String): List<String>
}

