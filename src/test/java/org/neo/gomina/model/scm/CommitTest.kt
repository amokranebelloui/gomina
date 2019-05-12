package org.neo.gomina.model.scm

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.neo.gomina.model.version.Version
import java.time.LocalDateTime

class CommitTest {

    @Test
    fun test_match() {
        val c4 = Commit("56585", LocalDateTime.now(), author = "amokrane", message = "commit msg", newVersion = "0.9.5-SNAPSHOT")
        val c3 = Commit("56583", LocalDateTime.now(), author = "amokrane", message = "commit msg", newVersion = "0.9.5-SNAPSHOT")
        val c2 = Commit("56582", LocalDateTime.now(), author = "amokrane", message = "commit msg", release = "0.9.4")
        assertThat(c2.match(Version("0.9.4", "56582"), true)).isTrue()
        assertThat(c2.match(Version("0.9.4", null), true)).isTrue()
        //println(release.match(Version("0.9.4", "null"), true))

        assertThat(c3.match(Version("0.9.5-SNAPSHOT", "56583"), true)).isTrue()
        assertThat(c3.match(Version("0.9.5-SNAPSHOT", "56584"), numberedRevisions = false)).isFalse()
        assertThat(c3.match(Version("0.9.5-SNAPSHOT", "56584"), numberedRevisions = true)).isTrue()

        assertThat(c4.match(Version("0.9.5-SNAPSHOT", "56584"), numberedRevisions = false)).isFalse()
        assertThat(c4.match(Version("0.9.5-SNAPSHOT", "56584"), numberedRevisions = true)).isFalse()
    }
}