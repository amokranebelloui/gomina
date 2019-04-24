package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.release.ReleaseService
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.activity
import org.neo.gomina.model.version.Version
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class ScmService {

    @Inject lateinit var scmRepos: ScmReposImpl
    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var events: Events

    fun reloadScmDetails(componentId: String, scm: Scm) {
        scmRepos.getScmDetails(scm).apply {
            componentRepo.editOwner(componentId, owner)
            componentRepo.editCriticality(componentId, critical)
            componentRepo.editArtifactId(componentId, artifactId)

            componentRepo.updateVersions(componentId,
                    latest?.let { Version(it, latestRevision) },
                    released?.let { Version(it, releasedRevision) }, changes)

            componentRepo.updateBranches(componentId, branches)
            componentRepo.updateDocFiles(componentId, docFiles)
            val prodEnvs = inventory.getProdEnvironments().map { it.id }
            val releases = events.releases(componentId, prodEnvs)
            val commitToRelease = ReleaseService.commitToRelease(commitLog, releases)

            val reference = LocalDateTime.now(Clock.systemUTC())
            val activity = commitLog.activity(reference)
            val lastCommit = commitLog.firstOrNull()?.date

            componentRepo.updateLastCommit(componentId, lastCommit)
            componentRepo.updateCommitActivity(componentId, activity)
            componentRepo.updateCommitToRelease(componentId, commitToRelease)
            componentRepo.updateCommitLog(componentId, commitLog)
        }
    }

    @Deprecated("Get from local database", ReplaceWith("componentRepo.getBranch(componentId, branchId)"))
    fun getBranch(scm: Scm, branchId: String): List<Commit> {
        return scmRepos.getBranch(scm, branchId)
    }

    // TODO Cache documents, for quicker serving
    fun getDocument(scm: Scm, docId: String): String? {
        return scmRepos.getDocument(scm, docId)?.let { Processor.process(it) }
    }
}