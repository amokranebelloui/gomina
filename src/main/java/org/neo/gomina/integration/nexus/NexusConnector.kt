package org.neo.gomina.integration.nexus

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.neo.gomina.model.version.Version
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

class NexusConnector(val host: String, val port:Int, private val releaseRepo: String, private val snapshotRepo: String? = null, isNexus3: Boolean = true) {

    private val httpclient = HttpClients.createDefault()

    private val repoPrefix = if (isNexus3) "repository" else "nexus/content/repositories"

    // FIXME Get SNAPSHOT Version
    // http://$host:$port/repository/snapshots/g1/g2/g3/$artifact/$version-SNAPSHOT/maven-metadata.xml

    @Throws(Exception::class)
    fun getContent(group: String, artifact: String, version: String? = null, classifier: String? = null, type: String? = null): String? {
        var v = version ?: lastVersion(group, artifact)
        return if (v != null) {
            //System.out.println("Version: " + v);
            val fullVersion = if (Version.isSnapshot(v)) explicitSnapshotVersion(group, artifact, v) else v
            //System.out.println("Version real: " + fullVersion);
            val fileName = StringBuilder("$artifact-$fullVersion").apply {
                classifier?.let { append("-$classifier") }
                append(".${type ?: "jar"}")
            }.toString()
            //val fileName = "$artifact-$fullVersion-$classifier.$type"
            val repoName = if (Version.isSnapshot(v)) snapshotRepo else releaseRepo
            val url = "http://$host:$port/$repoPrefix/$repoName/${group.replace(".", "/")}/$artifact/$v/$fileName"
            System.out.println("URL: " + url)

            httpGet(url)
        }
        else null
    }

    @Throws(Exception::class)
    private fun lastVersion(group: String, artifact: String): String? {
        val xpath = XPathFactory.newInstance().newXPath().compile("/metadata/versioning/versions/version[last()]/text()")
        val url = "http://$host:$port/$repoPrefix/$snapshotRepo/${group.replace(".", "/")}/$artifact/maven-metadata.xml"
        return httpGet(url)
                ?.let { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(it.toByteArray())) }
                ?.let { xpath.evaluate(it) }
    }

    @Throws(Exception::class)
    private fun explicitSnapshotVersion(group: String, artifact: String, version: String?): String? {
        val xpath = XPathFactory.newInstance().newXPath().compile("/metadata/versioning/snapshotVersions/snapshotVersion/value[1]/text()")
        val url = "http://$host:$port/$repoPrefix/$snapshotRepo/${group.replace(".", "/")}/$artifact/$version/maven-metadata.xml" // FIXME Nexus3 ??
        //val url = "http://$host:$port/$repoPrefix/$snapshotRepo/${group.replace(".", "/")}/$artifact/maven-metadata.xml"
        return httpGet(url)
                ?.let { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(it.toByteArray())) }
                ?.let { xpath.evaluate(it) }
    }

    @Throws(IOException::class)
    private fun httpGet(url: String): String? {
        return httpclient.execute(HttpGet(url)).use { response ->
            val entity = response.entity
            IOUtils.toString(entity.content).also { EntityUtils.consume(entity) }
        }
    }
}