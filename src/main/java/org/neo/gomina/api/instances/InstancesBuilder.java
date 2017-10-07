package org.neo.gomina.api.instances;

import org.neo.gomina.model.inventory.Environment;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.Service;
import org.neo.gomina.model.monitoring.EnvMonitoring;
import org.neo.gomina.model.monitoring.Monitoring;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstancesBuilder {

    @Inject private Inventory inventory;
    @Inject private Monitoring monitoring;

    public List<Instance> getInstances() {
        List<Instance> instances = new ArrayList<>();
        for (Environment environment : inventory.getEnvironments()) {
            EnvMonitoring envMonitoring = monitoring.getFor(environment.id);
            if (environment.services != null) {
                for (Service service : environment.services) {
                    for (InvInstance envInstance : service.instances) {
                        Map<String, Object> indicators = envMonitoring.getForInstance(envInstance.id);
                        indicators = indicators != null ? indicators : new HashMap<>();
                        instances.add(build(environment, service, envInstance, indicators));
                    }
                }
            }
        }
        return instances;
    }

    private Instance build(Environment env, Service service, InvInstance envInstance, Map<String, Object> indicators) {
        Instance instance = new Instance();
        instance.env = env.id;
        instance.id = env.id + "-" + envInstance.id;
        instance.type = service.type;
        instance.service = service.svc;
        instance.name = envInstance.id;
        instance.deployHost = envInstance.host;
        instance.deployFolder = envInstance.folder;

        instance.project = (String)indicators.get("project"); // FIXME ???

        instance.host = (String)indicators.get("host");
        instance.confCommited = (Boolean) indicators.get("confCommited");
        instance.version = (String)indicators.get("version");
        instance.revision = String.valueOf(indicators.get("revision"));

        instance.status = (String)indicators.get("status");
        instance.jmx = (Integer) indicators.get("jmx");
        instance.busVersion = (String)indicators.get("busVersion");
        instance.coreVersion = (String)indicators.get("coreVersion");
        instance.quickfixPersistence = (String)indicators.get("quickfixPersistence");
        instance.redisHost = (String)indicators.get("redisHost");
        instance.redisPort = (Integer) indicators.get("redisPort");
        instance.redisMasterHost = (String)indicators.get("redisMasterHost");
        instance.redisMasterPort = (Integer)indicators.get("redisMasterPort");
        instance.redisMasterLink = (Boolean) indicators.get("redisMasterLink");
        instance.redisMasterLinkDownSince = (String)indicators.get("redisMasterLinkDownSince");
        instance.redisOffset = (Integer) indicators.get("redisOffset");
        instance.redisOffsetDiff = (Integer) indicators.get("redisOffsetDiff");
        instance.redisMaster = (Boolean) indicators.get("redisMaster");
        instance.redisRole = (String)indicators.get("redisRole");
        instance.redisRW = (String)indicators.get("redisRW");
        instance.redisMode = (String)indicators.get("redisMode");
        instance.redisStatus = (String)indicators.get("redisStatus");
        instance.redisSlaveCount = (Integer) indicators.get("redisSlaveCount");
        instance.redisClientCount = (Integer) indicators.get("redisClientCount");

        return instance;
    }

}
