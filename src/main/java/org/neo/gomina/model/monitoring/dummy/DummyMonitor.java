package org.neo.gomina.model.monitoring.dummy;

import javax.inject.Inject;

public class DummyMonitor {

    @Inject private DummyMonitorThread dummyMonitorThread;

    @Inject
    public void init() {
        dummyMonitorThread.start();
    }
}
