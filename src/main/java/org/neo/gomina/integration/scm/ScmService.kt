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
        val svnRepo: String = project.svnRepo
        val svnUrl: String = project.svnUrl
        return scmCache.get("$svnRepo-$svnUrl", fromCache) { scmRepos.getScmDetails(svnRepo, svnUrl) }
    }

    fun getDocument(project: Project, docId: String): String? {
        // TODO Consider scmType, noSCM
        val file = scmRepos.getDocument(project.svnRepo, project.svnUrl, docId)
        return Processor.process(file)
    }
}