package org.neo.gomina.module;

import com.google.inject.*;
import com.google.inject.name.Names;
import org.apache.commons.lang3.StringUtils;
import org.neo.gomina.api.diagram.DiagramApi;
import org.neo.gomina.api.envs.EnvBuilder;
import org.neo.gomina.api.envs.EnvsApi;
import org.neo.gomina.api.instances.InstancesApi;
import org.neo.gomina.api.projects.ProjectsApi;
import org.neo.gomina.api.realtime.NotificationsApi;
import org.neo.gomina.core.instances.InstancesExt;
import org.neo.gomina.core.projects.ProjectsExt;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.project.file.FileProjects;
import org.neo.gomina.model.scm.ScmConfig;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.dummy.DummySonarConnector;
import org.neo.gomina.model.sonar.http.HttpSonarConnector;
import org.neo.gomina.module.config.Config;
import org.neo.gomina.module.config.ConfigLoader;
import org.neo.gomina.plugins.inventory.InventoryPlugin;
import org.neo.gomina.plugins.monitoring.MonitoringPlugin;
import org.neo.gomina.plugins.monitoring.zmq.ZmqMonitorConfig;
import org.neo.gomina.plugins.monitoring.zmq.ZmqMonitorThreads;
import org.neo.gomina.plugins.project.ProjectPlugin;
import org.neo.gomina.plugins.scm.ScmPlugin;
import org.neo.gomina.plugins.scm.connectors.ConfigScmRepos;
import org.neo.gomina.plugins.sonar.SonarPlugin;
import org.neo.gomina.plugins.ssh.DumbSshConnector;
import org.neo.gomina.plugins.ssh.SshConfig;
import org.neo.gomina.plugins.ssh.SshConnector;
import org.neo.gomina.plugins.ssh.connector.SshClient;
import org.neo.gomina.plugins.ssh.impl.OnDemandSshConnector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class GominaModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();

        Config config;
        try {
            ConfigLoader configLoader = new ConfigLoader();
            config = configLoader.load();
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot load config", e);
        }

        // Security
        bind(File.class).annotatedWith(Names.named("passwords")).toInstance(new File(config.passwordsFile));
        bind(Passwords.class).in(Scopes.SINGLETON);

        // Inventory
        bind(File.class).annotatedWith(Names.named("projects.file"))
                .toInstance(new File(config.inventory.get("projectsFile")));
        bind(String.class).annotatedWith(Names.named("inventory.dir"))
                .toInstance(config.inventory.get("inventoryDir"));
        bind(String.class).annotatedWith(Names.named("inventory.filter"))
                .toInstance(config.inventory.get("inventoryFilter"));
        // FIXME Type
        
        bind(Projects.class).to(FileProjects.class).in(Scopes.SINGLETON);
        bind(Inventory.class).to(FileInventory.class).in(Scopes.SINGLETON);
        bind(InventoryPlugin.class).in(Scopes.SINGLETON);
        bind(ProjectPlugin.class).in(Scopes.SINGLETON);

        // Monitoring
        bind(MonitoringPlugin.class).in(Scopes.SINGLETON);
        bind(ZmqMonitorConfig.class).toInstance(config.zmqMonitoring);
        bind(ZmqMonitorThreads.class).asEagerSingleton();

        // SCM
        bind(ScmConfig.class).toInstance(config.scm);
        bind(ScmRepos.class).to(ConfigScmRepos.class).in(Scopes.SINGLETON);
        bind(ScmPlugin.class).in(Scopes.SINGLETON);

        // Sonar
        bind(String.class).annotatedWith(Names.named("sonar.url")).toInstance(config.sonar.url);
        Class<? extends SonarConnector> sonarConnector = StringUtils.equals(config.sonar.mode, "dummy")
                ? DummySonarConnector.class
                : HttpSonarConnector.class;
        bind(SonarConnector.class).to(sonarConnector).in(Scopes.SINGLETON);
        bind(SonarPlugin.class).in(Scopes.SINGLETON);

        // SSH
        bind(SshConfig.class).toInstance(config.ssh);
        bind(SshClient.class).in(Scopes.SINGLETON);
        bind(SshConnector.class).to(OnDemandSshConnector.class).in(Scopes.SINGLETON);
        bind(DumbSshConnector.class).in(Scopes.SINGLETON);

        // API
        bind(EnvBuilder.class).in(Scopes.SINGLETON);

        bind(new TypeLiteral<ArrayList<ProjectsExt>>(){}).annotatedWith(Names.named("projects.plugins"))
                .toProvider(new Provider<ArrayList<ProjectsExt>>() {
            @Inject private ProjectPlugin projectPlugin;
            @Inject private SonarPlugin sonarPlugin;
            @Inject private ScmPlugin scmPlugin;
            
            @Override public ArrayList<ProjectsExt> get() {
                return new ArrayList<>(Arrays.asList(
                        projectPlugin,
                        sonarPlugin,
                        scmPlugin
                ));
            }
        });
        bind(new TypeLiteral<ArrayList<InstancesExt>>(){}).annotatedWith(Names.named("instances.plugins"))
                .toProvider(new Provider<ArrayList<InstancesExt>>() {
                    @Inject private InventoryPlugin inventoryPlugin;
                    @Inject private ScmPlugin scmPlugin;
                    @Inject private DumbSshConnector sshConnector;
                    @Inject private MonitoringPlugin monitoringPlugin;

                    @Override public ArrayList<InstancesExt> get() {
                        return new ArrayList<>(Arrays.asList(
                                inventoryPlugin,
                                scmPlugin,
                                sshConnector,
                                monitoringPlugin
                        ));
                    }
                });

        // Vertx API
        bind(EnvsApi.class).in(Scopes.SINGLETON);
        bind(ProjectsApi.class).in(Scopes.SINGLETON);
        bind(InstancesApi.class).in(Scopes.SINGLETON);
        bind(DiagramApi.class).in(Scopes.SINGLETON);
        bind(NotificationsApi.class).in(Scopes.SINGLETON);
    }

}
