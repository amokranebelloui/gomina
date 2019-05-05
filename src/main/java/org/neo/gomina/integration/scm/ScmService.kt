package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.integration.scm.metadata.ProjectMetadataMapper
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import org.neo.gomina.model.event.Event
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

    private val metadataMapper = ProjectMetadataMapper()

    fun reloadScmDetails(component: Component, scm: Scm) {
        val scmClient = scmRepos.getClient(scm)
        val trunk = scmClient.getTrunk() // FIXME Handle Branches
        val log = scmRepos.getLog(scmClient, trunk)

        componentRepo.updateCommitLog(component.id, log)

        val versions = log
                .filter {
                    val release = it.release
                    release != null && Version.isStable(release)
                }
                .map {
                    Event(
                            id = "${component.id}-${it.release}",
                            timestamp = it.date,
                            type = "version",
                            message = "Available",
                            componentId = component.id,
                            version = it.release
                    )
                }
        events.save(versions, "version")

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

        scmRepos.computeScmDetails(scm, log, scmClient).let { detail ->
            componentRepo.editArtifactId(component.id, detail.artifactId)

            componentRepo.updateVersions(component.id,
                    detail.latest?.let { Version(it, detail.latestRevision) },
                    detail.released?.let { Version(it, detail.releasedRevision) }, detail.changes)

            componentRepo.updateBranches(component.id, detail.branches)
            componentRepo.updateDocFiles(component.id, detail.docFiles)
        }

        val prodEnvs = inventory.getProdEnvironments().map { it.id }
        val releases = events.releases(component.id, prodEnvs)
        val commitToRelease = ReleaseService.commitToRelease(log, releases)

        val reference = LocalDateTime.now(Clock.systemUTC())
        val activity = log.activity(reference)
        val lastCommit = log.firstOrNull()?.date

        componentRepo.updateLastCommit(component.id, lastCommit)
        componentRepo.updateCommitActivity(component.id, activity)
        componentRepo.updateCommitToRelease(component.id, commitToRelease)
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