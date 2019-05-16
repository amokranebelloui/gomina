package org.neo.gomina.integration.maven

//import com.jcabi.aether.Aether
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.apache.maven.artifact.repository.MavenArtifactRepository
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout
import org.apache.maven.model.Model
import org.apache.maven.model.building.*
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.project.MavenProject
import org.neo.gomina.model.version.Version
//import org.sonatype.aether.util.artifact.DefaultArtifact
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
//import org.sonatype.aether.util.artifact.JavaScopes


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
/*
fun main1(args: Array<String>) {
    val model = loadModel(File("pom.xml"))
    model.dependencies.forEach {
        println(it)
    }
}

fun main(args: Array<String>) {
    //val pom = "/Users/Amokrane/Work/Code/Idea/rxjava-test/pom.xml"
    val pom = "pom.xml"
    val project = MavenProject(loadModel(File(pom))).apply { remoteArtifactRepositories = listOf(
            MavenArtifactRepository("maven-central", "http://repo1.maven.org/maven2/", DefaultRepositoryLayout(), ArtifactRepositoryPolicy(), ArtifactRepositoryPolicy())
    )}
    val aether = Aether(project, File(".temp/repository"))

    //val newSession = MavenRepositorySystemUtils.newSession()
    
    val artifacts = project.dependencies.flatMap { depend ->
        //println("DEP $depend")
        val defaultArtifact = DefaultArtifact(depend.groupId, depend.artifactId, depend.classifier, depend.type, depend.version)
        aether.resolve(defaultArtifact, JavaScopes.RUNTIME) { n, nodes ->
            //println(n.premanagedScope)
            true
        }
        //.map { art -> println("ART $art") }

    }
    artifacts.sortedBy { it.groupId }.forEach { println(it) }
}

fun loadProject(pomFile: File): MavenProject {
    val mavenReader = MavenXpp3Reader()
    return FileReader(pomFile).use { reader ->
        MavenProject(mavenReader.read(reader)?.also { it.pomFile = pomFile })
    }
}

fun loadModel(pomFile: File): Model {
    val modelBuilder = DefaultModelBuilderFactory().newInstance()
    //modelBuilder.setProfileSelector(DefaultProfileSelector())
    //modelBuilder.setModelProcessor(DefaultModelProcessor())
    val modelRequest = DefaultModelBuildingRequest()
    modelRequest.pomFile = pomFile
    val modelBuildingResult = modelBuilder.build(modelRequest)
    return modelBuildingResult.effectiveModel.also { it.pomFile = pomFile }
}

fun loadModel2(pomFile: File): Model {
    val mavenReader = MavenXpp3Reader()
    return FileReader(pomFile).use { reader ->
        mavenReader.read(reader)
    }
}
*/