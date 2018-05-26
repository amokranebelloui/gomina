package org.neo.gomina.plugins.scm.impl

import org.junit.Test
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.plugins.scm.ScmPlugin

class ScmPluginTest {

    @Test
    fun getSvnDetails() {
        class FileScmReposOverride : ScmRepos {
            override fun getDocument(id: String, svnUrl: String, docId: String): String? {
                TODO("not implemented")
            }

            override fun getScmDetails(id: String, svnUrl: String): ScmDetails {
                TODO("not implemented")
            }

        }

        val connector = ScmPlugin(FileScmReposOverride())

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")
        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")
        connector.getSvnDetails("repo", "OMS/Server/tradex-basketmanager")
    }

}