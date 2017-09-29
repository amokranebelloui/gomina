package org.neo.gomina.model.ssh;

import org.apache.commons.lang3.BooleanUtils;

import java.util.HashMap;
import java.util.Map;

public class DeployedInstances {

    private Map<String, Boolean> initialized = new HashMap<>();
    private Map<String, Map<String, DeployedInstance>> map = new HashMap<>();

    public DeployedInstance get(String env, String instanceName) {
        Map<String, DeployedInstance> servers = map.get(env);
        if (servers == null) {
            servers = new HashMap<>();
            map.put(env, servers);
        }

        DeployedInstance server = servers.get(instanceName);
        if (server == null) {
            server = new DeployedInstance(instanceName);
            servers.put(instanceName, server);
        }
        return server;
    }

    public void initialize(String env) {
        initialized.put(env, true);
    }

    public boolean isInitialized(String env) {
        return BooleanUtils.isTrue(initialized.get(env));
    }

}
