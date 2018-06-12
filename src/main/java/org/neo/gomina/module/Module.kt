package org.neo.gomina.module

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.name.Names.named
import org.neo.gomina.api.diagram.DiagramApi
import org.neo.gomina.api.envs.EnvsApi
import org.neo.gomina.api.events.EventsApi
import org.neo.gomina.api.events.EventsProviderFactory
import org.neo.gomina.api.hosts.HostsApi
import org.neo.gomina.api.instances.InstancesApi
import org.neo.gomina.api.projects.ProjectsApi
import org.neo.gomina.api.realtime.NotificationsApi
import org.neo.gomina.integration.monitoring.MonitoringEventsProvider
import org.neo.gomina.integration.jenkins.JenkinsConfig
import org.neo.gomina.integration.jenkins.JenkinsConnector
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.jenkins.jenkins.JenkinsConnectorImpl
import org.neo.gomina.integration.monitoring.Monitoring
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.scm.impl.ScmConfig
import org.neo.gomina.integration.scm.impl.ScmReposImpl
import org.neo.gomina.integration.sonar.SonarConfig
import org.neo.gomina.integration.sonar.SonarConnectors
import org.neo.gomina.integration.sonar.SonarService
import org.neo.gomina.integration.ssh.SshAnalysis
import org.neo.gomina.integration.ssh.SshClient
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.integration.zmqmonitoring.MonitoringMapper
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThreadPool
import org.neo.gomina.model.event.EventsProviderConfig
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.module.config.Config
import org.neo.gomina.module.config.ConfigLoader
import org.neo.gomina.persistence.jenkins.JenkinsConfigProvider
import org.neo.gomina.persistence.model.HostsFile
import org.neo.gomina.persistence.model.InventoryFile
import org.neo.gomina.persistence.model.ProjectsFile
import org.neo.gomina.persistence.scm.ScmConfigProvider
import org.neo.gomina.persistence.sonar.SonarConfigProvider
import org.neo.gomina.plugins.CustomMonitoringMapper
import org.neo.gomina.plugins.CustomSshAnalysis
import org.neo.gomina.plugins.PluginAssembler
import java.io.File

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
        bind(File::class.java).annotatedWith(named("passwords")).toInstance(File(config.passwordsFile!!))
        bind(Passwords::class.java).`in`(Scopes.SINGLETON)

        // Model
        bind(File::class.java).annotatedWith(named("projects.file")).toInstance(File(config.inventory.projectsFile))
        bind(File::class.java).annotatedWith(named("hosts.file")).toInstance(File(config.inventory.hostsFile))
        bind(String::class.java).annotatedWith(named("inventory.dir")).toInstance(config.inventory.inventoryDir)
        bind(String::class.java).annotatedWith(named("inventory.filter")).toInstance(config.inventory.inventoryFilter)

        bind(Projects::class.java).to(ProjectsFile::class.java).`in`(Scopes.SINGLETON)
        bind(Hosts::class.java).to(HostsFile::class.java).`in`(Scopes.SINGLETON)
        bind(Inventory::class.java).to(InventoryFile::class.java).`in`(Scopes.SINGLETON)

        // Monitoring
        bind(Int::class.java).annotatedWith(named("monitoring.timeout")).toInstance(config.monitoring.timeout)
        bind(Monitoring::class.java).`in`(Scopes.SINGLETON)
        bind(ZmqMonitorThreadPool::class.java).`in`(Scopes.SINGLETON)

        // SCM
        bind(ScmConfig::class.java).toProvider(ScmConfigProvider::class.java)
        bind(ScmRepos::class.java).to(ScmReposImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ScmService::class.java).`in`(Scopes.SINGLETON)

        // Jenkins
        bind(JenkinsConfig::class.java).toProvider(JenkinsConfigProvider::class.java)
        bind(JenkinsConnector::class.java).to(JenkinsConnectorImpl::class.java).`in`(Scopes.SINGLETON)
        bind(JenkinsService::class.java).`in`(Scopes.SINGLETON)

        // Sonar
        bind(SonarConfig::class.java).toProvider(SonarConfigProvider::class.java)
        bind(SonarConnectors::class.java).`in`(Scopes.SINGLETON)
        bind(SonarService::class.java).`in`(Scopes.SINGLETON)

        // SSH
        bind(SshClient::class.java).`in`(Scopes.SINGLETON)
        bind(SshOnDemandConnector::class.java).`in`(Scopes.SINGLETON)
        bind(SshService::class.java).`in`(Scopes.SINGLETON)

        // EventRepo
        //bind(MonitoringEventsProvider::class.java).`in`(Scopes.SINGLETON)

        // Elastic
        bind(typeLiteral<List<@JvmSuppressWildcards EventsProviderConfig>>()).toInstance(config.events.all())
        install(FactoryModuleBuilder()
                //.implement(EventsProvider::class.java, ElasticEvents::class.java)
                .build(EventsProviderFactory::class.java))

        // Custom
        bind(PluginAssembler::class.java).`in`(Scopes.SINGLETON)
        bind(SshAnalysis::class.java).to(CustomSshAnalysis::class.java).`in`(Scopes.SINGLETON)
        bind(MonitoringMapper::class.java).to(CustomMonitoringMapper::class.java).`in`(Scopes.SINGLETON)

        // Vertx API
        bind(ProjectsApi::class.java).`in`(Scopes.SINGLETON)
        bind(EnvsApi::class.java).`in`(Scopes.SINGLETON)
        bind(HostsApi::class.java).`in`(Scopes.SINGLETON)
        bind(InstancesApi::class.java).`in`(Scopes.SINGLETON)
        bind(EventsApi::class.java).`in`(Scopes.SINGLETON)
        bind(DiagramApi::class.java).`in`(Scopes.SINGLETON)
        bind(NotificationsApi::class.java).`in`(Scopes.SINGLETON)
    }

}

inline fun <reified T> typeLiteral() = object : TypeLiteral<T>() { }