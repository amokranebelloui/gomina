package org.neo.gomina.integration.scm.svn

import org.fest.assertions.Assertions
import org.junit.Test
import org.neo.gomina.integration.scm.Branch
import org.neo.gomina.integration.scm.Trunk
import org.tmatesoft.svn.core.SVNDirEntry

class TmateSoftSvnClientTest {

    @Test
    fun testSvn() {
        val svnClient = TmateSoftSvnClient("file:////Users/Shared/SvnRepo/svn-repo-demo")

        val svnLog = svnClient.getLog("svn-project2", Trunk, "0", 100)
        svnLog.forEach { println(it) }
        Assertions.assertThat(svnLog.size).isEqualTo(10)
    }

    @Test
    fun testBranches() {
        val svnClient = TmateSoftSvnClient("file:////Users/Shared/SvnRepo/svn-repo-demo")
        val repo = svnClient.repository

        println(repo.getRepositoryRoot(true))
        println(repo.getRepositoryUUID(true))

        val entries = arrayListOf<SVNDirEntry>()
        repo.getDir("/svn-project1/branches", -1, null, entries)
        entries.forEach {
                println("dir: $it ${it?.javaClass}")
                svnClient.getLog("svn-project1/", Branch("${it.name}"), "0", 100)
                        .forEach { println(" " + it) }
            }

    }
}