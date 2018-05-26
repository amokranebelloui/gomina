package org.neo.gomina.integration.scm.svn

import org.junit.Test

class TmateSoftSvnClientTest {

    @Test
    fun testSvn() {
        val svnClient = TmateSoftSvnClient("file:////Users/Amokrane/Work/SvnRepo/svn-repo-demo")

        val svnLog = svnClient.getLog("svn-project2", "0", 100)
        svnLog.forEach { println(it) }
    }

}