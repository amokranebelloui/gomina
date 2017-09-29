package org.neo.gomina.model.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Monitoring {

    private List<MonitoringListener> listeners = new CopyOnWriteArrayList<>();

    public Monitoring() {
        new MonitoringThread(this).start();
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
