package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.model.project.Scm
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class ScmService : ScmRepos {

    private val scmCache = Cache<ScmDetails>("scm") {
        it.branches = it.branches ?: emptyList()
        it.docFiles = it.docFiles ?: emptyList()
    }

    @Inject lateinit var scmRepos: ScmReposImpl

    override fun getScmDetails(scm: Scm): ScmDetails? {
        return scmCache.get(scm.id, true)
    }

    fun reloadScmDetails(scm: Scm) {
        scmCache.get(scm.id, false) { scmRepos.getScmDetails(scm) }
    }

    override fun getTrunk(scm: Scm): List<Commit> {
        return scmRepos.getTrunk(scm)
    }

    override fun getBranch(scm: Scm, branchId: String): List<Commit> {
        return scmRepos.getBranch(scm, branchId)
    }

    override fun getDocument(scm: Scm, docId: String): String? {
        return scmRepos.getDocument(scm, docId)?.let { Processor.process(it) }
    }
}