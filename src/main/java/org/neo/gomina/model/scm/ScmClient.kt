package org.neo.gomina.model.scm

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.issues.IssueProjects
import org.neo.gomina.model.issues.extractIssues
import org.neo.gomina.model.version.Version
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
        return if (version.isStable()) {
            version.version == this.version
        }
        else {
            // FIXME Revision as Int
            try {
                this.revision == version.revision ||
                        numberedRevisions && version.revision?.isNotBlank() == true && version.revision.toInt() > this.revNum
            }
            catch (e: Exception) {
                logger.error(e.message)
                false
            }
        }
    }
    companion object {
        private val logger = LogManager.getLogger(Commit::class.java)
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

data class ScmBranch(
        var name: String,
        var origin: String? = null,
        var originRevision: String? = null
)

interface ScmClient {

    fun getTrunk(): String = ""

    fun getBranches(): List<ScmBranch> = arrayListOf()

    /** Get log from HEAD to revision, max @count elements */
    fun getLog(branch: String, rev: String, count: Int): List<Commit>

    /** get file for a revision, HEAD is -1 **/
    fun getFile(branch: String, url: String, rev: String): String?

    fun listFiles(url: String, rev: String): List<String>
}

