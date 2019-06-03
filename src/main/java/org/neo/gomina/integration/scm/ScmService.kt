package org.neo.gomina.integration.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.Artifact
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
import org.neo.gomina.model.event.EventCategory
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

        componentRepo.cleanSnapshotVersions(component.id)
        libraries.cleanSnapshotVersions(component.id)

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

            override fun visitHead(commit: Commit, branch: String, isTrunk: Boolean) {
                println("--> visit head isTrunk=$isTrunk")
                val pomFile: String? = scmClient.getFile(branch, "pom.xml", commit.revision)
                val artifact = MavenUtils.extractArtifactId(pomFile)
                val latestVersion = latestVersion(listOf(commit), pomFile) // FIXME chgSignature

                if (latestVersion != null) {
                    componentRepo.addVersions(component.id, branch, listOf(VersionRelease(artifact, latestVersion, commit.date, branch)))
                }

                if (isTrunk) {

                    pomFile?.let { componentRepo.editArtifactId(component.id, artifact?.idOnly()) }

                    val docFiles = scmClient.listFiles("/", commit.revision).filter { it.endsWith(".md") }
                    componentRepo.updateDocFiles(component.id, docFiles)

                    if (component.hasMetadata) {
                        val metadata = scmClient.getFile(branch, "project.yaml", commit.revision)?.let { metadataMapper.map(it) }

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

                        if (latestVersion != null) {
                            metadata?.libraries?.mapNotNull { Artifact.parse(it) }?.let {
                                libraries.addUsage(component.id, latestVersion, it)
                            }
                        }
                    }
                }

                if (latestVersion != null) {
                    processLibraryUsage(component, latestVersion, pomFile)
                }
            }

            override fun visitVersion(commit: Commit, branch: String, isTrunk: Boolean, versionRelease: VersionRelease) {
                println("--> visit version ${component.id} isTrunk=$isTrunk $versionRelease")
                val pomFile: String? = scmClient.getFile(branch, "pom.xml", commit.revision)

                componentRepo.addVersions(component.id, branch, listOf(versionRelease)) // FIXME chgSignature

                if (versionRelease.artifact != null) {
                    libraries.addArtifact(versionRelease.artifact, versionRelease.version)
                }

                if (component.hasMetadata) {
                    val versionMetadata = scmClient.getFile(branch, "project.yaml", commit.revision)?.let { metadataMapper.map(it) }
                    versionMetadata?.libraries?.mapNotNull { Artifact.parse(it) }?.let {
                        libraries.addUsage(component.id, versionRelease.version, it)
                    }
                }

                processLibraryUsage(component, versionRelease.version, pomFile)

                val releaseEvent = Event(
                        id = "${component.id}-${versionRelease.version}",
                        timestamp = versionRelease.releaseDate,
                        group = EventCategory.VERSION,
                        type = "version", message = "Available",
                        componentId = component.id,
                        version = versionRelease.version.version)
                events.save(listOf(releaseEvent)) // FIXME chgSignature
            }

        }

        component.accept(scmClient, componentRepo, visitor)
    }

    private fun processLibraryUsage(component: Component, version: Version, pom: String?) {
        try {
            val dependencies = pom?.let { mavenDependencyResolver.dependencies(it) } ?: emptyList()
            libraries.addUsage(component.id, version, dependencies.map {
                Artifact.from(
                        it.artifact.groupId,
                        it.artifact.artifactId,
                        it.artifact.version,
                        type = it.artifact.extension,
                        classifier = it.artifact.classifier
                )
            })
        }
        catch (e: Exception) {
            logger.error("", e)
        }
    }

    private fun latestVersion(log: List<Commit>, pomFile: String?): Version? {
        val latest = MavenUtils.extractVersion(pomFile)
        val latestRevision = log.firstOrNull()?.revision
        return latest?.takeIf { latest.isNotBlank() }?.let { Version(it, latestRevision) }
    }

    // TODO Cache documents, for quicker serving + from branches ??
    fun getDocument(scm: Scm, docId: String): String? {
        val scmClient = scmClients.getClient(scm)
        val branch = scmClient.getTrunk() // Serve documents from branches ?
        return scmClient.getFile(branch, docId, "-1")?.let { Processor.process(it) }
    }
}