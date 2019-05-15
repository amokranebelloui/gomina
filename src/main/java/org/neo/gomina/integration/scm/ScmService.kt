package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.integration.scm.impl.ScmClients
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

    @Inject lateinit var scmClients: ScmClients
    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var interactionsRepo: InteractionsRepository
    @Inject lateinit var libraries: Libraries
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var events: Events

    private val metadataMapper = ProjectMetadataMapper()

    // FIXME Number of commits to process ?? save last processed revision by branch ?

    fun reloadScmDetails(component: Component, scm: Scm) {
        logger.info("Reload SCM Details for ${component.id} $scm")

        //val lastRevision = componentRepo.getCommitLog(component.id).firstOrNull()?.revision
        //logger.info("Last processed ${component.id} -> $lastRevision")

        val scmClient = scmClients.getClient(scm)
        val pomFile: String? = scmClient.getFile("pom.xml", "-1")

        // Commit Log
        val branches = scmClient.getBranches()
        branches.forEach { branch ->
            componentRepo.updateCommitLog(component.id, scmClient.getLog(branch.name, "0", 100))
        }
        componentRepo.updateBranches(component.id, branches)

        // Information
        val artifactId = MavenUtils.extractArtifactId(pomFile)
        pomFile?.let { componentRepo.editArtifactId(component.id, artifactId) }
        componentRepo.updateDocFiles(component.id, scmClient.listFiles("/", "-1").filter { it.endsWith(".md") })

        // Trunk
        val trunk = scmClient.getTrunk() // FIXME Handle Branches
        val log = scmClient.getLog(trunk, "0", 100)
        val versionsLog = log.filter { c -> c.version != null && Version.isStable(c.version) }

        // Versions
        val versions: List<VersionRelease> = versionsLog
                .mapNotNull { commit -> commit.version?.let { Triple(
                        MavenUtils.extractArtifactId(scmClient.getFile("pom.xml", commit.revision)),
                        it,
                        commit.date
                )}}
                //.filter { (_, release, _) -> Version.isStable(release) }
                .map { (artifactId, release, date) -> VersionRelease(artifactId, Version(release), date) }

        println(component.id + " " + versions)
        
        val releasedVersion = releasedVersion(log)
        val changes = commitCountTo(log, releasedVersion?.revision)?.let { it - 1 }
        val latestVersion = latestVersion(log, pomFile)

        componentRepo.updateVersions(component.id, latestVersion, releasedVersion, changes)
        componentRepo.updateVersions(component.id, versions)
        versions.forEach {
            if (it.artifactId?.isNotBlank() == true) {
                libraries.addArtifactId(it.artifactId, it.version)
            }
        }

        // Metadata
        if (component.hasMetadata) {
            run {
                val metadata = scmClient.getFile("project.yaml", "-1")?.let { metadataMapper.map(it) }

                componentRepo.editInceptionDate(component.id, metadata?.inceptionDate)
                componentRepo.editOwner(component.id, metadata?.owner)
                componentRepo.editCriticity(component.id, metadata?.criticity)

                interactionsRepo.update("metadata", listOf(
                        Interactions(component.id,
                                metadata?.api?.map { Function(it.name, it.type) } ?: emptyList(),
                                metadata?.dependencies?.map { FunctionUsage(it.name, it.type, it.usage?.let { Usage(it) }) }
                                        ?: emptyList()
                        )
                ))
            }

            versionsLog.forEach { versionCommit ->
                val versionMetadata = scmClient.getFile("project.yaml", versionCommit.revision)?.let { metadataMapper.map(it) }
                versionMetadata?.libraries?.mapNotNull { ArtifactId.tryWithVersion(it) }?.let {
                    if (latestVersion != null) {
                        libraries.addUsage(component.id, latestVersion, it)
                    }
                    // FIXME Previous version static dependencies
                }
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
        return latest?.takeIf { latest.isNotBlank() }?.let { Version(it, latestRevision) }
    }

    private fun releasedVersion(log: List<Commit>): Version? {
        val firstReleaseCommit = log.firstOrNull { it.version != null && Version.isStable(it.version) }
        //val releasedRevision = log.firstOrNull { StringUtils.isNotBlank(it.newVersion) }?.revision
        return if (firstReleaseCommit?.version != null) {
            Version(firstReleaseCommit.version, firstReleaseCommit.revision)
        }
        else null
    }

    private fun commitCountTo(logEntries: List<Commit>, to: String?): Int? {
        logEntries.map { it.revision }.withIndex().forEach { (count, revision) ->
            if (StringUtils.equals(revision, to)) return count
        }
        return null
    }

    @Deprecated("Get from local database", ReplaceWith("componentRepo.getBranch(componentId, branchId)"))
    fun getBranch(scm: Scm, branchId: String): List<Commit> {
        val scmClient = scmClients.getClient(scm)
        // FIXME Number of commits to process ??
        return scmClient.getLog(branchId, "0", 100)
    }

    // TODO Cache documents, for quicker serving + from branches ??
    fun getDocument(scm: Scm, docId: String): String? {
        val scmClient = scmClients.getClient(scm)
        return scmClient.getFile(docId, "-1")?.let { Processor.process(it) }
    }
}