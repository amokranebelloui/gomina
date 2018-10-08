package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.model.project.Project
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class ScmService {

    private val scmCache = Cache<ScmDetails>("scm") {
        it.branches = it.branches ?: emptyList()
        it.docFiles = it.docFiles ?: emptyList()
    }

    @Inject lateinit var scmRepos: ScmRepos

    fun getScmDetails(project: Project, fromCache: Boolean = false): ScmDetails? {
        // TODO Consider scmType, noSCM
        return project.scm?.let { scm ->
            scmCache.get("${scm.id}", fromCache) { scmRepos.getScmDetails(scm) }
        }
    }

    fun getBranch(project: Project, branchId: String): List<Commit> {
        // TODO Consider scmType, noSCM
        return project.scm
                ?.let { scmRepos.getBranch(it, branchId) } ?: emptyList()
    }

    fun getDocument(project: Project, docId: String): String? {
        // TODO Consider scmType, noSCM
        return project.scm
                ?.let{ scmRepos.getDocument(it, docId) }
                ?.let { Processor.process(it) }
    }
}