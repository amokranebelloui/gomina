package org.neo.gomina.plugins.scm.impl

import org.junit.Test
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.ScmRepos

class ScmPluginTest {

    @Test
    fun getSvnDetails() {
        class FileScmReposOverride : ScmRepos {
            override fun getTrunk(scm: Scm): List<Commit> {
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

        /*
        val connector = ScmService()
        connector.scmRepos = FileScmReposOverride()

        connector.getScmDetails(Scm(url = "OMS/Server/tradex-fixin"))
        connector.getScmDetails(Scm(url = "OMS/Server/tradex-fixin"))

        connector.getScmDetails(Scm(url = "OMS/Server/tradex-fixin"))
        connector.getScmDetails(Scm(url = "OMS/Server/tradex-basketmanager"))
        */
    }

}