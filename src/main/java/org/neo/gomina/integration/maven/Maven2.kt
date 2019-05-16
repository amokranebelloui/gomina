package org.neo.gomina.integration.maven

import org.apache.maven.model.Model
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.resolution.ModelResolver
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.project.ProjectModelResolver
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.RequestTrace
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.impl.ArtifactResolver
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.impl.RemoteRepositoryManager
import org.eclipse.aether.internal.impl.DefaultArtifactResolver
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager
import org.eclipse.aether.repository.ArtifactRepository
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import java.util.*
import org.eclipse.aether.util.filter.DependencyFilterUtils
import java.io.File


fun newRepositorySystem(): RepositorySystem {
    /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
    locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
    locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)

    locator.setErrorHandler(object : DefaultServiceLocator.ErrorHandler() {
        override fun serviceCreationFailed(type: Class<*>?, impl: Class<*>?, exception: Throwable?) {
            exception!!.printStackTrace()
        }
    })

    return locator.getService(RepositorySystem::class.java)
}

/*
fun newRepositorySystem(): RepositorySystem {
    return org.eclipse.aether.examples.manual.ManualRepositorySystemFactory.newRepositorySystem()
    // return org.eclipse.aether.examples.guice.GuiceRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.sisu.SisuRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.plexus.PlexusRepositorySystemFactory.newRepositorySystem();
}
*/

fun newRepositorySystemSession(system: RepositorySystem): DefaultRepositorySystemSession {
    val session = MavenRepositorySystemUtils.newSession()

    //val localRepo = LocalRepository(".temp/repository")
    val localRepo = LocalRepository("/Users/Amokrane/.m2/repository")
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))

    //session.setTransferListener(ConsoleTransferListener())
    //session.setRepositoryListener(ConsoleRepositoryListener())

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session
}

val remote = RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build()
val local = LocalRepository("/Users/Amokrane/.m2/repository")

fun newRepositories(system: RepositorySystem, session: RepositorySystemSession): List<ArtifactRepository> {
    return listOf(remote, local)
}

fun main(args: Array<String>) {
    println("------------------------------------------------------------")

    val system = newRepositorySystem()
    val session = newRepositorySystemSession(system)


    val remoteRepoManager = DefaultRemoteRepositoryManager()
    remoteRepoManager
    val modelResolver = ProjectModelResolver(session, null, system,
            remoteRepoManager,
            listOf(remote),
            ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT, null)


    val model = loadModel(File("pom.xml"), modelResolver)
    val project = MavenProject(model)
    val ds = model.dependencies.map { Dependency(DefaultArtifact(it.groupId, it.artifactId, it.classifier, it.type, it.version), JavaScopes.RUNTIME) }
    val mds = model.dependencyManagement?.dependencies?.map { Dependency(DefaultArtifact(it.groupId, it.artifactId, it.classifier, it.type, it.version), JavaScopes.RUNTIME) } ?: emptyList()

    val artifact = DefaultArtifact("org.eclipse.aether:aether-impl:1.0.0.v20140518")
    //val artifact = DefaultArtifact(model.id)

    val classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME)

    //val ds2 = listOf<>()

    ds.forEach { println(it) }

    println("----------- 2")

    val collectRequest = CollectRequest(ds, mds, listOf(remote))
    //val collectRequest = CollectRequest()
    //collectRequest.setRoot(Dependency(artifact, JavaScopes.COMPILE))
    //collectRequest.setRepositories(newRepositories(system, session))

    val dependencyRequest = DependencyRequest(collectRequest, classpathFlter)

    val artifactResults = system.resolveDependencies(session, dependencyRequest).getArtifactResults()

    for (artifactResult in artifactResults) {
        println(artifactResult.artifact.toString() + " resolved to " + artifactResult.getArtifact().getFile())
    }
}

fun loadModel(pomFile: File, modelResolver: ModelResolver): Model {
    val modelBuilder = DefaultModelBuilderFactory().newInstance()
    //val modelResolver = makeModelResolver()
    //modelBuilder.setProfileSelector(DefaultProfileSelector())
    //modelBuilder.setModelProcessor(DefaultModelProcessor())
        
    val modelRequest = DefaultModelBuildingRequest()
    modelRequest.pomFile = pomFile
    modelRequest.modelResolver = modelResolver
    val modelBuildingResult = modelBuilder.build(modelRequest)
    return modelBuildingResult.effectiveModel.also { it.pomFile = pomFile }
}

fun invoke(  o: Object,  method : String): Any {
    return o.getClass().getMethod( method ).invoke( o );
}

/*
fun makeModelResolver(): ModelResolver {

    val artifactResolver = DefaultArtifactResolver()

    val projectBuildingRequest = invoke( project, "getProjectBuildingRequest" ) as ProjectBuildingRequest

    val c = Class.forName("org.apache.maven.repository.internal.DefaultModelResolver");
    val ct = c.getConstructor(arrayOf(RepositorySystemSession.class,
            RequestTrace.class, String.class,
            ArtifactResolver.class, RemoteRepositoryManager.class,
            List.class));
    ct.setAccessible(true);
    return (org.apache.maven.model.resolution.ModelResolver) ct.newInstance(arrayOf(
            projectBuildingRequest.getRepositorySession(),
            null, null, artifactResolver, remoteRepositoryManager,
            project.getRemoteProjectRepositories()));

}
        */