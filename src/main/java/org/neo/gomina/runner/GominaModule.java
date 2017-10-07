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
import org.neo.gomina.model.monitoring.zmq.ZmqMonitorThread;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.project.file.FileProjects;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scm.file.FileScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.impl.CachedScmConnector;
import org.neo.gomina.model.scminfo.impl.DefaultScmConnector;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.dummy.DummySonarConnector;

public class GominaModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();

        // Inventory
        bind(Inventory.class).to(FileInventory.class).in(Scopes.SINGLETON);
        bind(Projects.class).to(FileProjects.class).in(Scopes.SINGLETON);

        // Monitoring
        bind(Monitoring.class).in(Scopes.SINGLETON);
        bind(DummyMonitorData.class).in(Scopes.SINGLETON);
        bind(DummyMonitorThread.class).asEagerSingleton();
        bind(ZmqMonitorThread.class).asEagerSingleton();

        // SCM
        bind(ScmRepos.class).to(FileScmRepos.class).in(Scopes.SINGLETON);
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
