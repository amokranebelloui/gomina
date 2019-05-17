package org.neo.gomina.integration.scm.svn

import org.fest.assertions.Assertions
import org.junit.Test

class TmateSoftSvnClientTest {

    val root = "file:////Users/Shared/SvnRepo/svn-repo-demo/"
    val projectUrl = "svn-project1"

    @Test
    fun testSvn() {
        val svnClient = TmateSoftSvnClient(root, projectUrl)

        val svnLog = svnClient.getLog("trunk", "0", 100)
        svnLog.forEach { println(it) }
        Assertions.assertThat(svnLog.size).isGreaterThan(16)
    }

    @Test
    fun testListFiles() {
        val svnClient = TmateSoftSvnClient(root, projectUrl)
        svnClient.listFiles("/", "-1").forEach { println(it) }
    }

    @Test
    fun testGetFile() {
        val svnClient = TmateSoftSvnClient(root, projectUrl)
        svnClient.getFile("trunk", "README.md", "-1")?.let { println(it) }
    }

    @Test
    fun testBranches() {
        val svnClient = TmateSoftSvnClient(root, projectUrl)
        svnClient.getBranches().forEach { println("branch: $it") }
    }
}