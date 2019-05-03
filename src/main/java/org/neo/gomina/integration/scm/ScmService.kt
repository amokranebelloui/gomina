package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.integration.scm.impl.CommitDecorator
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.integration.scm.metadata.ProjectMetadataMapper
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
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
    @Inject lateinit var interactionsRepo: InteractionsRepository
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var events: Events

    @Inject lateinit var commitDecorator: CommitDecorator

    private val metadataMapper = ProjectMetadataMapper()

    fun reloadScmDetails(component: Component, scm: Scm) {
        val scmClient = scmRepos.getClient(scm)
        val trunk = scmClient.getTrunk()
        val log = scmClient.getLog(trunk, "0", 100).map { commitDecorator.flag(it, scmClient) }

        val scmDetails = scmRepos.computeScmDetails(scm, log, scmClient) // if (scm.url.isNotBlank())

        if (component.hasMetadata) {
            val metadata = scmClient.getFile("project.yaml", "-1")?.let { metadataMapper.map(it) }

            componentRepo.editInceptionDate(component.id, metadata?.inceptionDate)
            componentRepo.editOwner(component.id, metadata?.owner)
            componentRepo.editCriticity(component.id, metadata?.criticity)

            interactionsRepo.update("metadata", listOf(
                    Interactions(component.id,
                            metadata?.api?.map { Function(it.name, it.type) } ?: emptyList(),
                            metadata?.dependencies?.map { FunctionUsage(it.name, it.type, it.usage?.let { Usage(it) }) } ?: emptyList()
                    )
            ))
        }

        scmDetails.apply {
            componentRepo.editArtifactId(component.id, artifactId)

            componentRepo.updateVersions(component.id,
                    latest?.let { Version(it, latestRevision) },
                    released?.let { Version(it, releasedRevision) }, changes)

            componentRepo.updateBranches(component.id, branches)
            componentRepo.updateDocFiles(component.id, docFiles)
            val prodEnvs = inventory.getProdEnvironments().map { it.id }
            val releases = events.releases(component.id, prodEnvs)
            val commitToRelease = ReleaseService.commitToRelease(commitLog, releases)

            val reference = LocalDateTime.now(Clock.systemUTC())
            val activity = commitLog.activity(reference)
            val lastCommit = commitLog.firstOrNull()?.date

            componentRepo.updateLastCommit(component.id, lastCommit)
            componentRepo.updateCommitActivity(component.id, activity)
            componentRepo.updateCommitToRelease(component.id, commitToRelease)
            componentRepo.updateCommitLog(component.id, commitLog)
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