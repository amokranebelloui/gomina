package org.neo.gomina.model.monitoring.dummy;

import org.junit.Test;
import org.neo.gomina.model.monitoring.dummy.DummyMonitorData;

import java.util.Map;

public class DummyMonitorDataTest {

    @Test
    public void getFor() throws Exception {
        DummyMonitorData repository = new DummyMonitorData();
        Map<String, Map<String, Object>> envMonitoring = repository.getFor("uat");
        for (Map<String, Object> mon : envMonitoring.values()) {
            System.out.println(mon);
        }
    }

}