package org.neo.gomina.integration.maven

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class MavenIdTest {

    @Test
    fun extractVersion() {
        assertThat(MavenId.from("com.neo.gomina:maven-plugin")).isEqualTo(MavenId("com.neo.gomina", "maven-plugin"))
        assertThat(MavenId.from("com.neo.gomina:maven-plugin:2.0.0")).isEqualTo(MavenId("com.neo.gomina", "maven-plugin", "2.0.0"))
        assertThat(MavenId.from("maven-plugin")).isEqualTo(MavenId(artifactId = "maven-plugin"))
        assertThat(MavenId.from(" ")).isNull()
        assertThat(MavenId.from("")).isNull()
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