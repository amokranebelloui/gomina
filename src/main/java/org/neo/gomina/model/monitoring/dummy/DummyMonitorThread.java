package org.neo.gomina.model.monitoring.dummy;

import org.neo.gomina.model.inventory.Environment;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.Service;
import org.neo.gomina.model.monitoring.Monitoring;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DummyMonitorThread extends Thread {

    @Inject private Monitoring monitoring;

    @Inject private Inventory inventory;
    @Inject private DummyMonitorData dummyMonitorData;
    private Random random = new Random();

    @Inject
    public void init() {
        this.start();
    }

    @Override
    public void run() {
        for (Environment env : inventory.getEnvironments()) {
            Map<String, Map<String, Object>> envMon = dummyMonitorData.getFor(env.id);
            for (Map.Entry<String, Map<String, Object>> entry : envMon.entrySet()) {
                monitoring.notify(env.id, entry.getKey(), entry.getValue());
            }
        }

        while (true) {
            for (Environment env : inventory.getEnvironments()) {
                for (Service service : env.services) {
                    for (InvInstance instance : service.instances) {
                        Map<String, Object> map = new HashMap<>();
                        int i = random.nextInt(15);
                        String status =
                                i == 0 ? "LIVE" :
                                i == 1 ? "LOADING" :
                                i == 2 ? "DOWN" : null;
                        if (status != null) {
                            map.put("status", status);
                            monitoring.notify(env.id, instance.id, map);
                        }
                    }
                }
            }
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}