package org.neo.gomina.integration.scm.dummy

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.scm.Branch
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

    override fun getBranches(): List<Branch> {
        val projectData = getProjectData(url)
        val branches = projectData?.let { project ->
            project.keys.filter { key -> key.startsWith("branch") }.map { key -> Branch(key, originRevision = (project[key] as Map<String, Any>)["origin"] as String?) }
        } ?: emptyList()
        return listOf(Branch("trunk")) + branches
    }

    override fun getLog(branch: String, rev: String, count: Int): List<Commit> {
        try {
            val projectData = getProjectData(url)
            if (projectData != null) {
                val log = ArrayList<Map<String, Any>>()
                val commitLog = if (branch.startsWith("branch")) {
                    val branchData = projectData[branch] as Map<String, Any>
                    branchData["log"]
                }
                else {
                    projectData["log"]
                }
                for (commit in commitLog as List<Map<String, Any>>) {
                    log.add(commit)
                    if (StringUtils.equals(commit["revision"] as String, rev)) {
                        break
                    }
                }
                return log.map { buildFrom(it) }
            }
        } catch (e: Exception) {
            logger.error("Error retrieving SVN data for $url", e)
        }

        return ArrayList()
    }

    override fun getFile(url: String, rev: String): String? {
        val projectData = getProjectData(this.url)
        if (projectData != null) {
            val log = projectData["log"] as List<Map<String, Any>>?
            val commit = if (rev == "-1") (if (log?.isNotEmpty() == true) log[0] else null) else findRevision(log, rev)
            return commit?.let {
                when (url) {
                    "pom.xml" -> pomXml(commit)
                    "project.yaml" -> projectYaml(url, commit)
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
        val c = ArtifactId.tryWithGroup(commit["artifactId"] as String?)
        return """
            <project>
                ${c?.groupId?.let { "<groupId>$it</groupId>" }}
                ${c?.artifactId?.let { "<artifactId>$it</artifactId>" }}
                ${c?.version?.let { "<version>$it</version>" }}
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

    private fun buildFrom(map: Map<String, Any>): Commit {
        return Commit(
                revision = map["revision"] as String,
                date = LocalDateTime.parse(map["date"] as String, DateTimeFormatter.ISO_DATE_TIME),
                author = map["author"] as String?,
                message = map["message"] as String?,
                release = map["version"] as String?
        )
    }

    private fun getProjectData(url: String): Map<String, Any>? {
        return mapper.readValue<List<Map<String, Any>>>(File("datadummy/dummyscm.json"))
                .firstOrNull { StringUtils.equals(it["url"] as String, url) }
    }

}