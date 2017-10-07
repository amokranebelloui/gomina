package org.neo.gomina.model.monitoring.zmq;

import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.monitoring.Monitoring;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ZmqMonitorThreads {

    @Inject
    public ZmqMonitorThreads(ZmqMonitorConfig config, Monitoring monitoring, Inventory inventory) {
        if (config.connections != null) {
            List<String> subscriptions = inventory.getEnvironments().stream()
                    .map(e -> ".#HB." + e.id + ".")
                    .collect(Collectors.toList());

            for (ZmqMonitorConfig.Connection connection : config.connections) {
                ZmqMonitorThread thread = new ZmqMonitorThread(monitoring, connection.url, subscriptions);
                thread.start();
            }
        }
    }
}
