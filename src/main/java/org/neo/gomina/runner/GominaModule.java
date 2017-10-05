package org.neo.gomina.runner;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.neo.gomina.api.instances.InstanceRepository;
import org.neo.gomina.api.projects.ProjectDetailRepository;
import org.neo.gomina.model.inventory.InventoryRepository;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.model.monitoring.MonitoringRepository;
import org.neo.gomina.model.project.ProjectRepository;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmConnector;
import org.neo.gomina.model.scm.impl.CachedScmConnector;
import org.neo.gomina.model.scm.impl.DefaultScmConnector;
import org.neo.gomina.model.scm.impl.DummyScmClient;
import org.neo.gomina.model.scm.impl.TmateSoftSvnClient;
import org.neo.gomina.model.sonar.Sonar;
import org.neo.gomina.model.sonar.SonarDummyClient;

public class GominaModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();

        bind(Monitoring.class).in(Scopes.SINGLETON);
        bind(InventoryRepository.class).in(Scopes.SINGLETON);
        bind(ProjectRepository.class).in(Scopes.SINGLETON);
        bind(MonitoringRepository.class).in(Scopes.SINGLETON);

        bind(TmateSoftSvnClient.class).in(Scopes.SINGLETON);
        bind(DummyScmClient.class).in(Scopes.SINGLETON);
        bind(ScmClient.class).to(DummyScmClient.class).in(Scopes.SINGLETON); //
        bind(DefaultScmConnector.class).in(Scopes.SINGLETON);
        bind(CachedScmConnector.class).in(Scopes.SINGLETON);
        bind(ScmConnector.class).to(CachedScmConnector.class).in(Scopes.SINGLETON);
        bind(Sonar.class).to(SonarDummyClient.class).in(Scopes.SINGLETON);

        bind(InstanceRepository.class).in(Scopes.SINGLETON);
        bind(ProjectDetailRepository.class).in(Scopes.SINGLETON);
    }

}
