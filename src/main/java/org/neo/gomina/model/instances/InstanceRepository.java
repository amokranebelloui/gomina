package org.neo.gomina.model.instances;

import org.neo.gomina.model.inventory.Environment;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.InventoryRepository;
import org.neo.gomina.model.inventory.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class InstanceRepository {

    @Inject private InventoryRepository inventoryRepository;

    public List<Instance> getInstances() {
        List<Instance> instances = new ArrayList<>();
        for (String envName : inventoryRepository.getEnvs()) {
            Environment env = inventoryRepository.getEnvironment(envName);
            if (env.services != null) {
                for (Service service : env.services) {
                    for (InvInstance envInstance : service.instances) {
                        instances.add(build(envName, envInstance));
                    }
                }
            }
        }
        return instances;
    }

    private Instance build(String env, InvInstance envInstance) {
        Instance instance = new Instance();
        instance.env = env;
        instance.id = envInstance.id;
        instance.name = envInstance.id;
        return instance;
    }

}
