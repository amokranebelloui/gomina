package org.neo.gomina.model.maven

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

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