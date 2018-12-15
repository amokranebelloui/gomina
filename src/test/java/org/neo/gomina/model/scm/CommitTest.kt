package org.neo.gomina.model.scm

import org.junit.Test
import org.neo.gomina.model.version.Version

class CommitTest {

    @Test
    fun test_match() {
        val commit = Commit("56582", release = "0.9.4")
        println(commit.match(Version("0.9.4", "56582")))
        println(commit.match(Version("0.9.4", null)))
        println(commit.match(Version("0.9.4", "null")))
    }
}