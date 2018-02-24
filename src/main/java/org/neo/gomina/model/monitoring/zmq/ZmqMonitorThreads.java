package org.neo.gomina.model.monitoring.zmq;

import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.monitoring.Monitoring;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

public class ZmqMonitorThreads {

    @Inject
    public ZmqMonitorThreads(ZmqMonitorConfig config, Monitoring monitoring, Inventory inventory) {
        if (config.connections != null) {
            Collection<String> subscriptions = inventory.getEnvironments().stream()
                    .map(e -> ".#HB." + e.getId() + ".")
                    .collect(Collectors.toList());

            for (ZmqMonitorConfig.Connection connection : config.connections) {
                ZmqMonitorThread thread = new ZmqMonitorThread(monitoring, connection.url, subscriptions);
                thread.start();
            }
        }
    }
}
