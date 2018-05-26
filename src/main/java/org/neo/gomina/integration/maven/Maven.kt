package org.neo.gomina.integration.maven

import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

object MavenUtils {

    private val logger = LogManager.getLogger(MavenUtils::class.java)

    fun extractVersion(pom: String?): String? {
        return if (pom != null) {
            try {
                val doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(ByteArrayInputStream(pom.toByteArray(StandardCharsets.UTF_8)))
                XPathFactory.newInstance().newXPath()
                        .compile("/project/version/text()")
                        .evaluate(doc)
            }
            catch (e: Exception) {
                logger.warn("Cannot parse pom", e)
                null
            }
        }
        else null
    }

}
