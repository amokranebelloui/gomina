package org.neo.gomina.integration.maven

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class ArtifactIdTest {

    @Test
    fun extractVersion() {
        assertThat(ArtifactId.tryWithGroup("com.neo.gomina:maven-plugin")).isEqualTo(ArtifactId("com.neo.gomina", "maven-plugin"))
        assertThat(ArtifactId.tryWithGroup("com.neo.gomina:maven-plugin:2.0.0")).isEqualTo(ArtifactId("com.neo.gomina", "maven-plugin", "2.0.0"))
        assertThat(ArtifactId.tryWithGroup("maven-plugin")).isEqualTo(ArtifactId(artifactId = "maven-plugin"))
        assertThat(ArtifactId.tryWithGroup(" ")).isNull()
        assertThat(ArtifactId.tryWithGroup("")).isNull()
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