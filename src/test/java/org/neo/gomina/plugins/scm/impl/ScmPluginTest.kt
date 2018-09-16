package org.neo.gomina.plugins.scm.impl

import org.junit.Test
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.scm.impl.ScmRepo
import org.neo.gomina.model.project.Project

class ScmPluginTest {

    @Test
    fun getSvnDetails() {
        class FileScmReposOverride : ScmRepos {
            override fun get(id: String): ScmRepo? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDocument(id: String, svnUrl: String, docId: String): String? {
                TODO("not implemented")
            }

            override fun getScmDetails(id: String, svnUrl: String): ScmDetails {
                TODO("not implemented")
            }

        }

        val connector = ScmService()
        connector.scmRepos = FileScmReposOverride()



        connector.getScmDetails(Project(id = "fixin", svnRepo = "repo", svnUrl = "OMS/Server/tradex-fixin"))
        connector.getScmDetails(Project(id = "fixin", svnRepo = "repo", svnUrl = "OMS/Server/tradex-fixin"))

        connector.getScmDetails(Project(id = "fixin", svnRepo = "repo", svnUrl = "OMS/Server/tradex-fixin"))
        connector.getScmDetails(Project(id = "basket", svnRepo = "repo", svnUrl = "OMS/Server/tradex-basketmanager"))
    }

}