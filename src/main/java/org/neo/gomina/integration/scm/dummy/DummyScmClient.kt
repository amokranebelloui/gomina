package org.neo.gomina.integration.scm.dummy

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.model.scm.ScmBranch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DummyScmClient : ScmClient {

    companion object {
        private val logger = LogManager.getLogger(DummyScmClient::class.java)
    }

    private val mapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)


    val url: String
    
    constructor(url: String) {
        this.url = url
    }

    override fun getTrunk(): String {
        return "trunk"
    }

    override fun getBranches(): List<ScmBranch> {
        val projectData = getProjectData(url)
        val branches = projectData?.let { project ->
            project.keys.filter { key -> key.startsWith("branch") }.map { key -> ScmBranch(key, originRevision = (project[key] as Map<String, Any>)["origin"] as String?) }
        } ?: emptyList()
        return listOf(ScmBranch("trunk")) + branches
    }

    override fun getLog(branch: String, rev: String, count: Int): List<Commit> {
        try {
            val projectData = getProjectData(url)
            if (projectData != null) {
                val log = ArrayList<Map<String, Any>>()
                val commitData = commitDataForBranch(branch, projectData)
                for (commit in commitData) {
                    log.add(commit)
                    if (StringUtils.equals(commit["revision"] as String, rev)) {
                        break
                    }
                }
                return log.map { buildFrom(it, branch) }
            }
        } catch (e: Exception) {
            logger.error("Error retrieving SVN data for $url", e)
        }

        return ArrayList()
    }

    private fun commitDataForBranch(branch: String, projectData: Map<String, Any>): List<Map<String, Any>> {
        return if (branch.startsWith("branch")) {
            val branchData = projectData[branch] as Map<String, Any>
            branchData["log"]
        }
        else {
            projectData["log"]
        }
        as List<Map<String, Any>>
    }

    override fun getFile(branch: String, url: String, rev: String): String? {
        val projectData = getProjectData(this.url)
        if (projectData != null) {
            /*
            val completeLog = getBranches().flatMap { branch ->
                commitDataForBranch(branch.name, projectData)
            }
            */
            val log = commitDataForBranch(branch, projectData)
            val commit = if (rev == "-1") log.firstOrNull() else findRevision(log, rev)
            return commit?.let { c ->
                when (url) {
                    "pom.xml" -> pomXml(c)
                    "project.yaml" -> projectYaml(url, c)
                    else -> null
                }
            }
        }
        return null
    }

    override fun listFiles(url: String, rev: String): List<String> {
        return listOf("README.md")
    }

    private fun pomXml(commit: Map<String, Any>): String {
        val c = commit["artifactId"]?.let { Artifact.parse(it as String) }
        return """
            <project>
                <modelVersion>4.0.0</modelVersion>
                ${c?.groupId?.let { "<groupId>$it</groupId>" }}
                ${c?.artifactId?.let { "<artifactId>$it</artifactId>" }}
                ${c?.version?.let { "<version>$it</version>" }}
                <dependencies>

                    <dependency>
                        <groupId>javax.inject</groupId>
                        <artifactId>javax.inject</artifactId>
                        <version>1</version>
                    </dependency>

                    <dependency>
                        <groupId>com.google.inject</groupId>
                        <artifactId>guice</artifactId>
                        <version>3.0</version>
                    </dependency>

                </dependencies>
            </project>

        """.trimMargin()
    }

    private fun projectYaml(url: String, commit: Map<String, Any>): String {
        val mesh = commit["mesh"] as String
        val language = commit["language"] as String
        return """
            id: $url

            owner: amokrane.belloui@gmail.com
            criticity: 3

            libraries:
              - org.demo:service-mesh:$mesh
              - $language

        """.trimIndent()
    }

    private fun findRevision(log: List<Map<String, Any>>?, rev: String): Map<String, Any>? {
        return log?.firstOrNull { StringUtils.equals(it["revision"] as String, rev) }
    }

    private fun buildFrom(commitData: Map<String, Any>, branch: String): Commit {
        val c = commitData["artifactId"]?.let { Artifact.parse(it as String) }
        return Commit(
                revision = commitData["revision"] as String,
                date = LocalDateTime.parse(commitData["date"] as String, DateTimeFormatter.ISO_DATE_TIME),
                author = commitData["author"] as String?,
                branches = listOf(branch),
                message = commitData["message"] as String?,
                version = c?.version
        )
    }

    private fun getProjectData(url: String): Map<String, Any>? {
        return mapper.readValue<List<Map<String, Any>>>(File("datadummy/dummyscm.json"))
                .firstOrNull { StringUtils.equals(it["url"] as String, url) }
    }

}