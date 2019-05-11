package org.neo.gomina.model.issues

import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.impl.CommitDecorator
import javax.inject.Inject

class IssueProjects {

    companion object {
        private val logger = LogManager.getLogger(CommitDecorator::class.java)
    }

    lateinit var issuePattern: Regex

    @Inject
    fun init(@Named("jira.projects") jiraProjects: List<String>) {
        val projects = jiraProjects.joinToString(separator = "|")
        issuePattern = "($projects)-[0-9]+".toRegex()
        logger.info("Issue Tracker projects: $projects regexp: $issuePattern")

    }

}

fun String.extractIssues(issues: IssueProjects) = issues.issuePattern.findAll(this).map { it.value }.toList()
