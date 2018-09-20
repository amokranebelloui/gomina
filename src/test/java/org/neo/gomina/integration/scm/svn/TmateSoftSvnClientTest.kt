package org.neo.gomina.integration.scm.svn

import org.fest.assertions.Assertions
import org.junit.Test

class TmateSoftSvnClientTest {

    @Test
    fun testSvn() {
        val svnClient = TmateSoftSvnClient("file:////Users/Shared/SvnRepo/svn-repo-demo", "svn-project2")

        val svnLog = svnClient.getLog("trunk", "0", 100)
        svnLog.forEach { println(it) }
        Assertions.assertThat(svnLog.size).isEqualTo(10)
    }

    @Test
    fun testBranches() {
        val svnClient = TmateSoftSvnClient("file:////Users/Shared/SvnRepo/svn-repo-demo", "svn-project1")
        svnClient.getBranches().forEach { println("branch: $it") }
    }
}