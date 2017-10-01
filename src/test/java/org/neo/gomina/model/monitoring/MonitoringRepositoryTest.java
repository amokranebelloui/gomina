package org.neo.gomina.model.monitoring;

import org.junit.Test;

public class MonitoringRepositoryTest {

    @Test
    public void getFor() throws Exception {
        MonitoringRepository repository = new MonitoringRepository();
        EnvMonitoring envMonitoring = repository.getFor("uat");
        for (String instance : envMonitoring.getInstances()) {
            System.out.println(envMonitoring.getForInstance(instance));
        }
    }

}