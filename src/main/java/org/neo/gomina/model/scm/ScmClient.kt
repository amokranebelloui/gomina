package org.neo.gomina.model.scm

import org.neo.gomina.model.issues.IssueProjects
import org.neo.gomina.model.issues.extractIssues
import org.neo.gomina.model.version.Version
import org.neo.gomina.model.version.isStableVersion
import java.time.LocalDateTime

data class Commit (
    val revision: String,
    val date: LocalDateTime,
    val author: String?,
    val message: String?,

    val branches: List<String> = emptyList(),

    val version: String? = null
) {

    val revNum: Int get() = this.revision.toInt()

    fun issues(projects: IssueProjects): List<String> {
        return message?.extractIssues(projects) ?: emptyList()
    }

    fun match(version: Version, numberedRevisions: Boolean): Boolean {
        // FIXME Revision as Int
        return this.revision == version.revision ||
                this.version?.isStableVersion() == true && this.version == version.version ||
                numberedRevisions && version.revision?.isNotBlank() == true && version.revision.toInt() > this.revNum
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
    return this.map { it.date }
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
    fun getFile(branch: String, url: String, rev: String): String?

    fun listFiles(url: String, rev: String): List<String>
}

