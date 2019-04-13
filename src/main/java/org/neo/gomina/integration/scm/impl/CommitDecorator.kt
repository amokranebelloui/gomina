package org.neo.gomina.integration.scm.impl

import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import javax.inject.Inject

class CommitDecorator {

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

    fun flag(commit: Commit, scmClient: ScmClient): Commit {
        // FIXME Detect build system
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin] prepare release")) {
            val pom = scmClient.getFile("pom.xml", commit.revision)
            commit.release = MavenUtils.extractVersion(pom)
        }
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin]")) {
            val pom = scmClient.getFile("pom.xml", commit.revision)
            commit.newVersion = MavenUtils.extractVersion(pom)
        }

        commit.issues = commit.message?.let { msg ->
            issuePattern.findAll(msg).map { it.value }
        }?.toList() ?: emptyList()

        return commit // FIXME Make immutable
    }
}


fun main(args: Array<String>) {
    val projects = "DEMO|TEST|SUPPORT"
    val pattern = "($projects)-?[0-9]+".toRegex()

    pattern.findAll("[DEMO-668] Some commit [DEMO-778]").also { println() }.forEach { print("${it.value} ") }
    pattern.findAll("{DEMO-668] Some commit [DEMO-778]").also { println() }.forEach { print("${it.value} ") }
    pattern.findAll("SUPPORT-040: Some commit for bugfixing").also { println() }.forEach { print("${it.value} ") }
    pattern.findAll("SUPPORT041: Some commit for bugfixing").also { println() }.forEach { print("${it.value} ") }
    pattern.findAll("DEMO-668 -DEMO-660- Some DF-12 commit").also { println() }.forEach { print("${it.value} ") }

}