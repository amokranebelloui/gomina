package org.neo.gomina.integration.maven

import org.apache.logging.log4j.LogManager
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

data class ArtifactId(val groupId: String? = null, val artifactId: String, val version: String? = null) {
    companion object {
        fun from(str: String): ArtifactId? {
            return str?.let {
                val split = str.split(":")
                when {
                    split.size > 2 -> ArtifactId(groupId = split[0], artifactId = split[1], version = split[2])
                    split.size == 2 -> ArtifactId(groupId = split[0], artifactId = split[1])
                    split.size == 1 && split[0].isNotBlank() -> ArtifactId(artifactId = split[0])
                    else -> null
                }
            }
        }
    }
}

object MavenUtils {

    private val logger = LogManager.getLogger(MavenUtils::class.java)

    fun extractVersion(pom: String?): String? {
        return if (pom != null) {
            try {
                XPathFactory.newInstance().newXPath()
                        .compile("/project/version/text()")
                        .evaluate(buildDoc(pom))
            } catch (e: Exception) {
                logger.warn("Cannot parse pom", e)
                null
            }
        }
        else null
    }

    fun extractArtifactId(pom: String?): String? {
        if (pom != null) {
            try {
                val doc = buildDoc(pom)
                val xpath = XPathFactory.newInstance().newXPath()
                val groupId = xpath.compile("/project/groupId/text()").evaluate(doc)
                val artifactId = xpath.compile("/project/artifactId/text()").evaluate(doc)
                if (groupId.isNotBlank() && artifactId.isNotBlank()) {
                    return "$groupId:$artifactId"
                }
            } catch (e: Exception) {
                logger.warn("Cannot parse pom", e)
            }
        }
        return null
    }

    private fun buildDoc(pom: String): Document? {
        val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(ByteArrayInputStream(pom.toByteArray(StandardCharsets.UTF_8)))
        return doc
    }

}
