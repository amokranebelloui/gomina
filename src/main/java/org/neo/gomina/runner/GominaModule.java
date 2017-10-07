package org.neo.gomina.runner;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
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
import org.neo.gomina.model.scm.ScmConfig;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scm.impl.ConfigScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.impl.CachedScmConnector;
import org.neo.gomina.model.scminfo.impl.DefaultScmConnector;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.dummy.DummySonarConnector;
import org.neo.gomina.runner.config.Config;
import org.neo.gomina.runner.config.ConfigLoader;

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
        bind(Passwords.class).in(Scopes.SINGLETON);

        // Inventory
        bind(Inventory.class).to(FileInventory.class).in(Scopes.SINGLETON);
        bind(Projects.class).to(FileProjects.class).in(Scopes.SINGLETON);

        // Monitoring
        bind(Monitoring.class).in(Scopes.SINGLETON);
        bind(DummyMonitorData.class).in(Scopes.SINGLETON);
        bind(DummyMonitorThread.class).asEagerSingleton();
        bind(ZmqMonitorConfig.class).toInstance(config.zmqMonitoring);
        bind(ZmqMonitorThreads.class).asEagerSingleton();

        // SCM
        bind(ScmConfig.class).toInstance(config.scm);
        bind(ScmRepos.class).to(ConfigScmRepos.class).in(Scopes.SINGLETON);
        bind(DefaultScmConnector.class).in(Scopes.SINGLETON);
        bind(CachedScmConnector.class).in(Scopes.SINGLETON);
        //bind(ScmConnector.class).to(CachedScmConnector.class).in(Scopes.SINGLETON);
        bind(ScmConnector.class).to(DefaultScmConnector.class).in(Scopes.SINGLETON);

        // Sonar
        bind(SonarConnector.class).to(DummySonarConnector.class).in(Scopes.SINGLETON);

        // TODO Jenkins
        // TODO Jiras
        // TODO Releases
        // TODO Dependencies

        // API
        bind(InstancesBuilder.class).in(Scopes.SINGLETON);
        bind(ProjectsBuilder.class).in(Scopes.SINGLETON);
    }

}
