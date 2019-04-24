package org.neo.gomina.integration.maven

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class ArtifactIdTest {

    @Test
    fun extractVersion() {
        assertThat(ArtifactId.from("com.neo.gomina:maven-plugin")).isEqualTo(ArtifactId("com.neo.gomina", "maven-plugin"))
        assertThat(ArtifactId.from("com.neo.gomina:maven-plugin:2.0.0")).isEqualTo(ArtifactId("com.neo.gomina", "maven-plugin", "2.0.0"))
        assertThat(ArtifactId.from("maven-plugin")).isEqualTo(ArtifactId(artifactId = "maven-plugin"))
        assertThat(ArtifactId.from(" ")).isNull()
        assertThat(ArtifactId.from("")).isNull()
    }
}

class MavenUtilsTest {

    @Test
    fun extractVersion() {
        val pom = """
                <project>
                    <version>12</version>
                </project>
            """

        assertThat(MavenUtils.extractVersion(pom)).isEqualTo("12")
        assertThat(MavenUtils.extractVersion(null)).isNull()
        assertThat(MavenUtils.extractVersion("")).isNull()
        assertThat(MavenUtils.extractVersion(" <xml>")).isNull()
    }
}