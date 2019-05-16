package org.neo.gomina.integration.maven

import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.apache.maven.model.Model
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.StringModelSource
import org.apache.maven.model.resolution.ModelResolver
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.project.ProjectModelResolver
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import javax.inject.Inject

data class MavenRepo(val id: String, val type: String, val url: String)

class MavenDependencyResolver {

    companion object {
        private val logger = LogManager.getLogger(MavenDependencyResolver::class.java)
    }

    private lateinit var system: RepositorySystem
    private lateinit var remoteRepositories: List<RemoteRepository>
    private lateinit var localRepository: LocalRepository

    @Inject
    fun init(@Named("maven.remote.repositories") remoteRepos: List<MavenRepo>,
             @Named("maven.local.repository") localRepo: String) {
        val locator = MavenRepositorySystemUtils.newServiceLocator().apply {
            this.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
            this.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
            this.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
            this.setErrorHandler(CustomErrorHandler())
        }
        system = locator.getService(RepositorySystem::class.java)

        remoteRepositories = remoteRepos.map { RemoteRepository.Builder(it.id, it.type, it.url).build() }
        localRepository = LocalRepository(localRepo)
    }

    fun dependencies(pom: String): List<ArtifactResult> {
        val session = MavenRepositorySystemUtils.newSession().also {
            it.localRepositoryManager = system.newLocalRepositoryManager(it, this.localRepository)
            //it.transferListener = ConsoleTransferListener()
            //it.repositoryListener = ConsoleRepositoryListener()
            //it.dependencyGraphTransformer = null // uncomment to generate dirty trees
        }
        val modelResolver = ProjectModelResolver(session, null, system,
                DefaultRemoteRepositoryManager(), remoteRepositories,
                ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT, null)

        val scope = JavaScopes.RUNTIME

        val model = loadModel(pom, modelResolver)
        val ds = model.dependencies.map { Dependency(toArtifact(it), scope) }
        val mds = model.dependencyManagement?.dependencies?.map { Dependency(toArtifact(it), scope) }

        val collectRequest = CollectRequest(ds, mds, remoteRepositories)
        //val collectRequest = CollectRequest(Dependency(DefaultArtifact(model.id), scope), remoteRepositories)
        val dependencyRequest = DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(scope))
        return system.resolveDependencies(session, dependencyRequest).artifactResults
    }

    fun loadModel(pom: String, modelResolver: ModelResolver): Model {
        val modelRequest = DefaultModelBuildingRequest().apply {
            //this.pomFile = pomFile
            this.modelSource = StringModelSource(pom)
            this.modelResolver = modelResolver
        }
        return DefaultModelBuilderFactory().newInstance()
                .build(modelRequest)
                .effectiveModel
        //.also { it.pomFile = pomFile }
    }

    private fun toArtifact(mavenDep: org.apache.maven.model.Dependency) =
            DefaultArtifact(mavenDep.groupId, mavenDep.artifactId, mavenDep.classifier, mavenDep.type, mavenDep.version)

    private class CustomErrorHandler : DefaultServiceLocator.ErrorHandler() {
        override fun serviceCreationFailed(type: Class<*>?, impl: Class<*>?, exception: Throwable?) {
            exception?.printStackTrace()
        }
    }

}

