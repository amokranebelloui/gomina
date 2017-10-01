package org.neo.gomina.runner;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.neo.gomina.api.instances.InstanceRepository;
import org.neo.gomina.api.projects.ProjectDetailRepository;
import org.neo.gomina.model.inventory.InventoryRepository;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.model.monitoring.MonitoringRepository;
import org.neo.gomina.model.project.ProjectRepository;
import org.neo.gomina.model.svn.SvnRepository;

public class GominaModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();

        bind(Monitoring.class).in(Scopes.SINGLETON);
        bind(InventoryRepository.class).in(Scopes.SINGLETON);
        bind(ProjectRepository.class).in(Scopes.SINGLETON);
        bind(MonitoringRepository.class).in(Scopes.SINGLETON);
        bind(SvnRepository.class).in(Scopes.SINGLETON);

        bind(InstanceRepository.class).in(Scopes.SINGLETON);
        bind(ProjectDetailRepository.class).in(Scopes.SINGLETON);
    }

}
