package org.neo.gomina.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.apache.commons.lang3.StringUtils;
import org.neo.gomina.api.diagram.DiagramApi;
import org.neo.gomina.api.envs.EnvBuilder;
import org.neo.gomina.api.instances.InstancesBuilder;
import org.neo.gomina.api.projects.ProjectsBuilder;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.model.monitoring.dummy.DummyMonitorData;
import org.neo.gomina.model.monitoring.dummy.DummyMonitorThread;
import org.neo.gomina.model.monitoring.zmq.ZmqMonitorConfig;
import org.neo.gomina.model.monitoring.zmq.ZmqMonitorThreads;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.project.file.FileProjects;
import org.neo.gomina.model.scm.ConfigScmRepos;
import org.neo.gomina.model.scm.ScmConfig;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.impl.CachedScmConnector;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.dummy.DummySonarConnector;
import org.neo.gomina.model.sonar.http.HttpSonarConnector;
import org.neo.gomina.model.ssh.SshClient;
import org.neo.gomina.model.sshinfo.SshConfig;
import org.neo.gomina.model.sshinfo.SshConnector;
import org.neo.gomina.model.sshinfo.impl.OnDemandSshConnector;
import org.neo.gomina.module.config.Config;
import org.neo.gomina.module.config.ConfigLoader;

import java.io.File;

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

        // Monitoring
        bind(Monitoring.class).in(Scopes.SINGLETON);
        bind(DummyMonitorData.class).in(Scopes.SINGLETON);
        bind(DummyMonitorThread.class).asEagerSingleton();
        bind(ZmqMonitorConfig.class).toInstance(config.zmqMonitoring);
        bind(ZmqMonitorThreads.class).asEagerSingleton();

        // SCM
        bind(ScmConfig.class).toInstance(config.scm);
        bind(ScmRepos.class).to(ConfigScmRepos.class).in(Scopes.SINGLETON);
        bind(CachedScmConnector.class).in(Scopes.SINGLETON);
        bind(ScmConnector.class).to(CachedScmConnector.class).in(Scopes.SINGLETON);

        // Sonar
        bind(String.class).annotatedWith(Names.named("sonar.url")).toInstance(config.sonar.url);
        Class<? extends SonarConnector> sonarConnector = StringUtils.equals(config.sonar.mode, "dummy")
                ? DummySonarConnector.class
                : HttpSonarConnector.class;
        bind(SonarConnector.class).to(sonarConnector).in(Scopes.SINGLETON);

        // SSH
        bind(SshConfig.class).toInstance(config.ssh);
        bind(SshClient.class).in(Scopes.SINGLETON);
        bind(SshConnector.class).to(OnDemandSshConnector.class).in(Scopes.SINGLETON);

        // API
        bind(EnvBuilder.class).in(Scopes.SINGLETON);
        bind(InstancesBuilder.class).in(Scopes.SINGLETON);
        bind(ProjectsBuilder.class).in(Scopes.SINGLETON);

        bind(DiagramApi.class).in(Scopes.SINGLETON);
    }

}
