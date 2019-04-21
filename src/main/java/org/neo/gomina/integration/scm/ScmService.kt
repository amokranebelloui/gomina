package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.release.ReleaseService
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.model.version.Version
import javax.inject.Inject

class ScmService : ScmRepos {

    @Inject lateinit var scmRepos: ScmReposImpl
    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var events: Events

    override fun getScmDetails(scm: Scm): ScmDetails? {
        return scmRepos.getScmDetails(scm)
    }

    fun reloadScmDetails(componentId: String, scm: Scm) {
        scmRepos.getScmDetails(scm).apply {
            componentRepo.editOwner(componentId, owner)
            componentRepo.editCriticality(componentId, critical)
            componentRepo.editArtifactId(componentId, mavenId)

            componentRepo.updateVersions(componentId,
                    latest?.let { Version(it, latestRevision) },
                    released?.let { Version(it, releasedRevision) }, changes)

            componentRepo.updateBranches(componentId, branches)
            componentRepo.updateDocFiles(componentId, docFiles)
            val prodEnvs = inventory.getProdEnvironments().map { it.id }
            val releases = events.releases(componentId, prodEnvs)
            val commitToRelease = ReleaseService.commitToRelease(commitLog, releases)
            componentRepo.updateCommitToRelease(componentId, commitToRelease)
            componentRepo.updateCommitLog(componentId, commitLog)
        }
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