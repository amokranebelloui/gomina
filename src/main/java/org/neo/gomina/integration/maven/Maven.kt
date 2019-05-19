package org.neo.gomina.integration.maven

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.VersionRelease
import org.neo.gomina.model.version.Version
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

data class ArtifactId(val groupId: String? = null, val artifactId: String, val version: String? = null) {
    companion object {
        fun withVersion(str: String): ArtifactId {
            val split = str.split(":")
            return when {
                split.size > 2 -> ArtifactId(groupId = split[0], artifactId = split[1], version = split[2])
                split.size == 2 -> ArtifactId(artifactId = split[0], version = split[1])
                split.size == 1 && split[0].isNotBlank() -> ArtifactId(artifactId = split[0])
                else -> ArtifactId(artifactId = split[0])
            }
        }
        fun withGroup(str: String): ArtifactId {
            val split = str.split(":")
            return when {
                split.size > 2 -> ArtifactId(groupId = split[0], artifactId = split[1], version = split[2])
                split.size == 2 -> ArtifactId(groupId = split[0], artifactId = split[1])
                split.size == 1 && split[0].isNotBlank() -> ArtifactId(artifactId = split[0])
                else -> ArtifactId(artifactId = split[0])
            }
        }
        fun tryWithVersion(str: String?): ArtifactId? {
            return str?.takeIf { it.isNotBlank() }?.let { withVersion(it) }
        }
        fun tryWithGroup(str: String?): ArtifactId? {
            return str?.takeIf { it.isNotBlank() }?.let { withGroup(it) }
        }
    }
    fun toStr() = "${groupId?.let { "$it:" } ?: ""}$artifactId${version?.let { ":$it" } ?: ""}"
    fun toStrWithoutVersion() = "${groupId?.let { "$it:" } ?: ""}$artifactId"
    fun getVersion() = this.version?.let { Version(it) }
}

data class Artifact(val groupId: String, val artifactId: String,
                    private val _version: String? = null,
                    private val _type: String? = null,
                    private val _classifier: String? = null) {

    val version = _version?.takeIf { it.isNotBlank() }
    val type = _type?.takeIf { it.isNotBlank() } ?: "jar"
    val classifier = _classifier?.takeIf { it.isNotBlank() }

    companion object {
        fun parse(str: String, containsVersion: Boolean = true): Artifact? {
            val s = str.split(":")
            return when {
                s.size >= 5 -> Artifact(groupId = s[0], artifactId = s[1], _type = s[2], _classifier = s[3], _version = s[4])
                s.size >= 4 && containsVersion  -> Artifact(groupId = s[0], artifactId = s[1], _type = s[2], _version = s[3])
                s.size >= 4 && !containsVersion -> Artifact(groupId = s[0], artifactId = s[1], _type = s[2], _classifier = s[3])
                s.size >= 3 && containsVersion  -> Artifact(groupId = s[0], artifactId = s[1], _version = s[2])
                s.size >= 3 && !containsVersion -> Artifact(groupId = s[0], artifactId = s[1], _type = s[2])
                s.size >= 2 -> Artifact(groupId = s[0], artifactId = s[1])
                else -> null
            }
        }
    }
    override fun toString() = "$groupId:$artifactId:$type${classifier?.let{":$it"}?:""}${version?.let{":$it"}?:""}"
    fun idOnly() = "$groupId:$artifactId"
    fun withoutVersion() = Artifact(this.groupId, this.artifactId, null, this.type, this.classifier)
    fun getVersion() = this.version?.let { Version(it) }
}

fun String.parseArtifact() = Artifact.parse(this)

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

    fun extractArtifactId(pom: String?): Artifact? {
        if (pom != null) {
            try {
                val doc = buildDoc(pom)
                val xpath = XPathFactory.newInstance().newXPath()
                val groupId = xpath.compile("/project/groupId/text()").evaluate(doc)
                val artifactId = xpath.compile("/project/artifactId/text()").evaluate(doc)
                val version = xpath.compile("/project/version/text()").evaluate(doc)
                val type = xpath.compile("/project/type/text()").evaluate(doc)
                val classifier = xpath.compile("/project/classifier/text()").evaluate(doc)
                if (groupId.isNotBlank() && artifactId.isNotBlank()) {
                    return Artifact(groupId, artifactId, version, type, classifier)
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
