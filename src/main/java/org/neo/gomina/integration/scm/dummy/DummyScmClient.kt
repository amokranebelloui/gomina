package org.neo.gomina.integration.scm.dummy

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient
import org.neo.gomina.integration.scm.Scope
import java.io.File
import java.util.*

class DummyScmClient : ScmClient {

    companion object {
        private val logger = LogManager.getLogger(DummyScmClient::class.java)
    }

    private val mapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun getLog(url: String, scope: Scope, rev: String, count: Int): List<Commit> {
        try {
            val projectData = getProjectData(url)
            if (projectData != null) {
                val log = ArrayList<Map<String, Any>>()
                for (commit in projectData["log"] as List<Map<String, Any>>) {
                    log.add(commit)
                    if (StringUtils.equals(commit["revision"] as String, rev)) {
                        break
                    }
                }
                return log.map { buildFrom(it) }
            }
        } catch (e: Exception) {
            logger.error("Error retrieving SVN data for " + url, e)
        }

        return ArrayList()
    }

    override fun getFile(url: String, rev: String): String? {
        val projectData = getProjectData(url.replace("/trunk/pom.xml", ""))
        if (projectData != null) {
            val log = projectData["log"] as List<Map<String, Any>>?
            val commit = if (rev == "-1") (if (log?.isNotEmpty() == true) log[0] else null) else findRevision(log, rev)
            return if (commit != null) sampleFile(commit["version"] as String) else null
        }
        return null
    }

    override fun listFiles(url: String, rev: String): List<String> {
        return listOf("README.md")
    }

    private fun sampleFile(version: String): String {
        return "<project><version>$version</version></project>"
    }

    private fun findRevision(log: List<Map<String, Any>>?, rev: String): Map<String, Any>? {
        return log?.firstOrNull { StringUtils.equals(it["revision"] as String, rev) }
    }

    private fun buildFrom(map: Map<String, Any>): Commit {
        return Commit(
            revision = map["revision"] as String,
            date = map["date"] as Date?,
            author = map["author"] as String?,
            message = map["message"] as String?
        )
    }

    private fun getProjectData(url: String): Map<String, Any>? {
        return mapper.readValue<List<Map<String, Any>>>(File("datadummy/projects.svn.yaml"))
                .firstOrNull { StringUtils.equals(it["url"] as String, url) }
    }

}