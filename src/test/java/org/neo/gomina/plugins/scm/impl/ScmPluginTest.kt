package org.neo.gomina.plugins.scm.impl

import org.junit.Test
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.scm.ScmRepo
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.scm.connectors.DummyScmClient

class ScmPluginTest {

    @Test
    fun getSvnDetails() {
        class FileScmReposOverride : ScmRepos {
            override fun getClient(id: String): ScmClient {
                return DummyScmClient()
            }

            override fun getRepo(id: String): ScmRepo? {
                return null
            }
        }

        val connector = ScmPlugin(FileScmReposOverride())

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")
        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin")
        connector.getSvnDetails("repo", "OMS/Server/tradex-basketmanager")
    }

}