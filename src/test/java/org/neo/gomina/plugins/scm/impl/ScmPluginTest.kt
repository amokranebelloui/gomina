package org.neo.gomina.plugins.scm.impl

import org.junit.Test
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.scm.impl.ScmRepo
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Scm

class ScmPluginTest {

    @Test
    fun getSvnDetails() {
        class FileScmReposOverride : ScmRepos {
            override fun get(id: String): ScmRepo? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDocument(scm: Scm, docId: String): String? {
                TODO("not implemented")
            }

            override fun getScmDetails(scm: Scm): ScmDetails {
                TODO("not implemented")
            }

            override fun getBranch(scm: Scm, branchId: String): List<Commit> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            
        }

        val connector = ScmService()
        connector.scmRepos = FileScmReposOverride()

        connector.getScmDetails(Project(id = "fixin", scm = Scm(repo = "repo", url = "OMS/Server/tradex-fixin")))
        connector.getScmDetails(Project(id = "fixin", scm = Scm(repo = "repo", url = "OMS/Server/tradex-fixin")))

        connector.getScmDetails(Project(id = "fixin", scm = Scm(repo = "repo", url = "OMS/Server/tradex-fixin")))
        connector.getScmDetails(Project(id = "basket", scm = Scm(repo = "repo", url = "OMS/Server/tradex-basketmanager")))
    }

}