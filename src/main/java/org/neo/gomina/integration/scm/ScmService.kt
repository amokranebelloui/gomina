package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.integration.maven.MavenDependencyResolver
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
import org.neo.gomina.model.scm.Branch
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
    @Inject lateinit var mavenDependencyResolver: MavenDependencyResolver
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
        //-----------------------

        val visitor = object : ComponentScmVisitor {
            override fun visitTrunk(commitLog: List<Commit>, latestVersion: Version?, releasedVersion: Version?, changes: Int?) {

                // Latest Available Versions
                componentRepo.updateVersions(component.id, latestVersion, releasedVersion, changes)

                // Activity Analysis
                val prodEnvs = inventory.getProdEnvironments().map { it.id }
                val releases = events.releases(component.id, prodEnvs)
                val commitToRelease = ReleaseService.commitToRelease(commitLog, releases)

                componentRepo.updateLastCommit(component.id, commitLog.firstOrNull()?.date)
                componentRepo.updateCommitActivity(component.id, commitLog.activity(LocalDateTime.now(Clock.systemUTC())))
                componentRepo.updateCommitToRelease(component.id, commitToRelease)
            }

            override fun visitHead(commit: Commit, isTrunk: Boolean) {
                println("--> visit head isTrunk=$isTrunk")
                if (isTrunk) {
                    val pomFile: String? = scmClient.getFile("pom.xml", commit.revision)

                    val artifactId = MavenUtils.extractArtifactId(pomFile)
                    pomFile?.let { componentRepo.editArtifactId(component.id, artifactId) }

                    val docFiles = scmClient.listFiles("/", commit.revision).filter { it.endsWith(".md") }
                    componentRepo.updateDocFiles(component.id, docFiles)

                    if (component.hasMetadata) {
                        val metadata = scmClient.getFile("project.yaml", commit.revision)?.let { metadataMapper.map(it) }

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

                        val latestVersion = latestVersion(listOf(commit), pomFile) // FIXME chgSignature
                        if (latestVersion != null) {
                            metadata?.libraries?.mapNotNull { ArtifactId.tryWithVersion(it) }?.let {
                                libraries.addUsage(component.id, latestVersion, it)
                            }
                        }
                    }

                    pomFile?.let { mavenDependencyResolver.dependencies(it) }?.forEach { println("-> $it") }
                }
            }

            override fun visitVersion(commit: Commit, isTrunk: Boolean, versionRelease: VersionRelease) {
                println("--> visit version ${component.id} isTrunk=$isTrunk $versionRelease")

                componentRepo.updateVersions(component.id, listOf(versionRelease)) // FIXME chgSignature

                if (versionRelease.artifactId?.isNotBlank() == true) {
                    libraries.addArtifactId(versionRelease.artifactId, versionRelease.version)
                }

                if (component.hasMetadata) {
                    val versionMetadata = scmClient.getFile("project.yaml", commit.revision)?.let { metadataMapper.map(it) }
                    versionMetadata?.libraries?.mapNotNull { ArtifactId.tryWithVersion(it) }?.let {
                        libraries.addUsage(component.id, versionRelease.version, it)
                    }
                }

                val releaseEvent = Event(
                        id = "${component.id}-${versionRelease.version}",
                        timestamp = versionRelease.releaseDate,
                        type = "version", message = "Available",
                        componentId = component.id,
                        version = versionRelease.version.version)
                events.save(listOf(releaseEvent), "version") // FIXME chgSignature
            }

        }

        component.accept(scmClient, componentRepo, visitor)
    }

    private fun latestVersion(log: List<Commit>, pomFile: String?): Version? {
        val latest = MavenUtils.extractVersion(pomFile)
        val latestRevision = log.firstOrNull()?.revision
        return latest?.takeIf { latest.isNotBlank() }?.let { Version(it, latestRevision) }
    }

    // TODO Cache documents, for quicker serving + from branches ??
    fun getDocument(scm: Scm, docId: String): String? {
        val scmClient = scmClients.getClient(scm)
        return scmClient.getFile(docId, "-1")?.let { Processor.process(it) }
    }
}