package org.neo.gomina.model.monitoring.zmq;

import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.monitoring.Monitoring;

import javax.inject.Inject;

public class ZmqMonitorThreads {

    @Inject
    public ZmqMonitorThreads(ZmqConfig config, Monitoring monitoring, Inventory inventory) {
        if (config.connections != null) {
            for (ZmqConnection connection : config.connections) {
                ZmqMonitorThread thread = new ZmqMonitorThread(monitoring, connection.url, inventory);
                thread.start();
            }
        }
    }
}
