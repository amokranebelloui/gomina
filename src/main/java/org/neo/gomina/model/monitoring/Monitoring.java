package org.neo.gomina.model.monitoring;

import org.neo.gomina.model.inventory.InventoryRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Monitoring {

    @Inject private InventoryRepository inventoryRepository;

    private List<MonitoringListener> listeners = new CopyOnWriteArrayList<>();

    @Inject
    public void init() {
        new MonitoringThread(inventoryRepository, this).start();
    }

    public void add(MonitoringListener listener) {
        this.listeners.add(listener);
    }

    public void notify(String instanceId, Map<String, String> newValues) {
        for (MonitoringListener listener : listeners) {
            listener.onPropertyChanged(instanceId, newValues);
        }
    }
}
