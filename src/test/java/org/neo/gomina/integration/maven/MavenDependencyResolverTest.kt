package org.neo.gomina.integration.maven

import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class MavenDependencyResolverTest {
    @Test
    fun testMavenDependencies() {
        val pom = FileUtils.readFileToString(File("pom.xml"), Charset.defaultCharset())

        val remote = MavenRepo("central", "default", "http://central.maven.org/maven2/")
        val local = "/Users/Amokrane/Applications/maven-libs"
        val resolver = MavenDependencyResolver().apply { init(listOf(remote), local) }

        resolver.dependencies(pom)
                .sortedBy { it.toString() }
                .forEach { println(it.artifact) }
    }
}