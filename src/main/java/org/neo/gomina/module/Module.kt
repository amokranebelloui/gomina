package org.neo.gomina.module

import com.google.inject.*
import com.google.inject.name.Names
import org.apache.commons.lang3.StringUtils
import org.neo.gomina.api.diagram.DiagramApi
import org.neo.gomina.api.envs.EnvBuilder
import org.neo.gomina.api.envs.EnvsApi
import org.neo.gomina.api.instances.InstancesApi
import org.neo.gomina.api.projects.ProjectsApi
import org.neo.gomina.api.realtime.NotificationsApi
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.file.FileInventory
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.project.file.FileProjects
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.module.config.Config
import org.neo.gomina.module.config.ConfigLoader
import org.neo.gomina.plugins.inventory.InventoryPlugin
import org.neo.gomina.plugins.jenkins.JenkinsConfig
import org.neo.gomina.plugins.jenkins.JenkinsPlugin
import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import org.neo.gomina.plugins.monitoring.zmq.ZmqMonitorConfig
import org.neo.gomina.plugins.monitoring.zmq.ZmqMonitorThreads
import org.neo.gomina.plugins.project.ProjectPlugin
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.scm.connectors.ConfigScmRepos
import org.neo.gomina.plugins.scm.connectors.ScmConfig
import org.neo.gomina.plugins.sonar.SonarConnector
import org.neo.gomina.plugins.sonar.SonarPlugin
import org.neo.gomina.plugins.sonar.connectors.DummySonarConnector
import org.neo.gomina.plugins.sonar.connectors.HttpSonarConnector
import org.neo.gomina.plugins.ssh.SshConfig
import org.neo.gomina.plugins.ssh.SshOnDemandConnector
import org.neo.gomina.plugins.ssh.SshPlugin
import org.neo.gomina.plugins.ssh.connector.SshClient
import java.io.File
import java.util.*

class GominaModule : AbstractModule() {

    override fun configure() {
        binder().requireExplicitBindings()

        val config: Config
        try {
            val configLoader = ConfigLoader()
            config = configLoader.load()
        } catch (e: Exception) {
            throw RuntimeException("Cannot load config", e)
        }

        // Security
        bind(File::class.java).annotatedWith(Names.named("passwords")).toInstance(File(config.passwordsFile!!))
        bind(Passwords::class.java).`in`(Scopes.SINGLETON)

        // Inventory
        bind(File::class.java).annotatedWith(Names.named("projects.file"))
                .toInstance(File(config.inventory!!["projectsFile"]))
        bind(String::class.java).annotatedWith(Names.named("inventory.dir"))
                .toInstance(config.inventory!!["inventoryDir"])
        bind(String::class.java).annotatedWith(Names.named("inventory.filter"))
                .toInstance(config.inventory!!["inventoryFilter"])
        // FIXME Type

        bind(Projects::class.java).to(FileProjects::class.java).`in`(Scopes.SINGLETON)
        bind(Inventory::class.java).to(FileInventory::class.java).`in`(Scopes.SINGLETON)
        bind(InventoryPlugin::class.java).`in`(Scopes.SINGLETON)
        bind(ProjectPlugin::class.java).`in`(Scopes.SINGLETON)

        // Monitoring
        bind(MonitoringPlugin::class.java).`in`(Scopes.SINGLETON)
        bind(ZmqMonitorConfig::class.java).toInstance(config.zmqMonitoring)
        bind(ZmqMonitorThreads::class.java).asEagerSingleton()

        // SCM
        bind(ScmConfig::class.java).toInstance(config.scm)
        bind(ScmRepos::class.java).to(ConfigScmRepos::class.java).`in`(Scopes.SINGLETON)
        bind(ScmPlugin::class.java).`in`(Scopes.SINGLETON)

        // Jenkins
        bind(JenkinsConfig::class.java).toInstance(config.jenkins)
        bind(JenkinsPlugin::class.java).`in`(Scopes.SINGLETON)

        // Sonar
        bind(String::class.java).annotatedWith(Names.named("sonar.url")).toInstance(config.sonar!!.url)
        val sonarConnector = if (StringUtils.equals(config.sonar!!.mode, "dummy"))
            DummySonarConnector::class.java
        else
            HttpSonarConnector::class.java
        bind(SonarConnector::class.java).to(sonarConnector).`in`(Scopes.SINGLETON)
        bind(SonarPlugin::class.java).`in`(Scopes.SINGLETON)

        // SSH
        bind(SshConfig::class.java).toInstance(config.ssh)
        bind(SshClient::class.java).`in`(Scopes.SINGLETON)
        bind(SshOnDemandConnector::class.java).`in`(Scopes.SINGLETON)
        bind(SshPlugin::class.java).`in`(Scopes.SINGLETON)

        // API
        bind(EnvBuilder::class.java).`in`(Scopes.SINGLETON)

        bind(object : TypeLiteral<ArrayList<ProjectsExt>>() {

        }).annotatedWith(Names.named("projects.plugins"))
                .toProvider(object : Provider<ArrayList<ProjectsExt>> {
                    @Inject private val projectPlugin: ProjectPlugin? = null
                    @Inject private val jenkinsPlugin: JenkinsPlugin? = null
                    @Inject private val sonarPlugin: SonarPlugin? = null
                    @Inject private val scmPlugin: ScmPlugin? = null

                    override fun get(): ArrayList<ProjectsExt> {
                        return ArrayList(Arrays.asList<ProjectsExt>(
                                projectPlugin,
                                jenkinsPlugin,
                                sonarPlugin,
                                scmPlugin
                        ))
                    }
                })
        bind(object : TypeLiteral<ArrayList<InstancesExt>>() {

        }).annotatedWith(Names.named("instances.plugins"))
                .toProvider(object : Provider<ArrayList<InstancesExt>> {
                    @Inject private val inventoryPlugin: InventoryPlugin? = null
                    @Inject private val scmPlugin: ScmPlugin? = null
                    @Inject private val sshConnector: SshPlugin? = null
                    @Inject private val monitoringPlugin: MonitoringPlugin? = null

                    override fun get(): ArrayList<InstancesExt> {
                        return ArrayList(Arrays.asList<InstancesExt>(
                                inventoryPlugin,
                                scmPlugin,
                                sshConnector,
                                monitoringPlugin
                        ))
                    }
                })

        // Vertx API
        bind(EnvsApi::class.java).`in`(Scopes.SINGLETON)
        bind(ProjectsApi::class.java).`in`(Scopes.SINGLETON)
        bind(InstancesApi::class.java).`in`(Scopes.SINGLETON)
        bind(DiagramApi::class.java).`in`(Scopes.SINGLETON)
        bind(NotificationsApi::class.java).`in`(Scopes.SINGLETON)
    }

}