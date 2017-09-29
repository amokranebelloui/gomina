package org.neo.gomina.model.monitoring;

import org.neo.gomina.model.inventory.Instance;
import org.neo.gomina.model.inventory.InventoryRepository;
import org.neo.gomina.model.inventory.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MonitoringThread extends Thread {

    private InventoryRepository inventoryRepository = new InventoryRepository();

    private Monitoring monitoring;

    public MonitoringThread(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    private Random random = new Random();

    @Override
    public void run() {
        while (true) {
            for (Service service : inventoryRepository.getEnvironment().services) {
                for (Instance instance : service.instances) {
                    Map<String, String> map = new HashMap<>();
                    int i = random.nextInt(15);
                    String status = i == 0 ? "LIVE" : i == 1 ? "LOADING" : i == 2 ? "DOWN" : null;
                    if (status != null) {
                        map.put("status", status);
                        monitoring.notify(instance.id, map);
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
