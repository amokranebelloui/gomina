package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.integration.scm.metadata.ProjectMetadataMapper
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.component.VersionRelease
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

    companion object {
        private val logger = LogManager.getLogger(ScmService::class.java)
    }

    @Inject lateinit var scmRepos: ScmReposImpl
    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var interactionsRepo: InteractionsRepository
    @Inject lateinit var libraries: Libraries
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var events: Events

    private val metadataMapper = ProjectMetadataMapper()

    fun reloadScmDetails(component: Component, scm: Scm) {
        logger.info("Reload SCM Details for ${component.id} $scm")

        val scmClient = scmRepos.getClient(scm)
        val trunk = scmClient.getTrunk() // FIXME Handle Branches
        val log = scmRepos.getLog(scmClient, trunk)
        val pomFile: String? = scmClient.getFile("pom.xml", "-1")

        // Commit Log
        componentRepo.updateCommitLog(component.id, log)
        componentRepo.updateBranches(component.id, scmClient.getBranches())

        // Information
        componentRepo.editArtifactId(component.id, MavenUtils.extractArtifactId(pomFile))
        componentRepo.updateDocFiles(component.id, scmClient.listFiles("/", "-1").filter { it.endsWith(".md") })

        // Versions
        val versions = log
                .mapNotNull { commit -> commit.release?.let { it to commit.date } }
                .filter { (release, _) -> Version.isStable(release) }
                .map { (release, date) -> VersionRelease(Version(release), date) }

        val latestVersion = latestVersion(log, pomFile)
        val releasedVersion = releasedVersion(log)
        val changes = commitCountTo(log, releasedVersion?.revision)

        componentRepo.updateVersions(component.id, latestVersion, releasedVersion, changes)
        componentRepo.updateVersions(component.id, versions)

        // Metadata
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

            metadata?.libraries?.mapNotNull { ArtifactId.tryWithVersion(it) }?.let {
                if (latestVersion != null) {
                    libraries.add(component.id, latestVersion, it)
                }
                // FIXME Previous version static dependencies
            }
        }

        // Activity Analysis
        val prodEnvs = inventory.getProdEnvironments().map { it.id }
        val releases = this.events.releases(component.id, prodEnvs)
        val commitToRelease = ReleaseService.commitToRelease(log, releases)

        componentRepo.updateLastCommit(component.id, log.firstOrNull()?.date)
        componentRepo.updateCommitActivity(component.id, log.activity(LocalDateTime.now(Clock.systemUTC())))
        componentRepo.updateCommitToRelease(component.id, commitToRelease)

        // Events
        val versionEvents = versions.map { Event(
                id = "${component.id}-${it.version}", timestamp = it.releaseDate, type = "version", message = "Available",
                componentId = component.id, version = it.version.version)
        }
        events.save(versionEvents, "version")
    }

    private fun latestVersion(log: List<Commit>, pomFile: String?): Version? {
        val latest = MavenUtils.extractVersion(pomFile)
        val latestRevision = log.firstOrNull()?.revision
        return latest?.let { Version(it, latestRevision) }
    }

    private fun releasedVersion(log: List<Commit>): Version? {
        val released = log.firstOrNull { StringUtils.isNotBlank(it.release) }?.release
        val releasedRevision = log.firstOrNull { StringUtils.isNotBlank(it.newVersion) }?.revision
        return released?.let { Version(it, releasedRevision) }
    }

    private fun commitCountTo(logEntries: List<Commit>, to: String?): Int? {
        logEntries.map { it.revision }.withIndex().forEach { (count, revision) ->
            if (StringUtils.equals(revision, to)) return count
        }
        return null
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