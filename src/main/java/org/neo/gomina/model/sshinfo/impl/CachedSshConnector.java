package org.neo.gomina.model.sshinfo.impl;

import org.neo.gomina.model.sshinfo.SshConnector;
import org.neo.gomina.model.sshinfo.SshDetails;

import java.util.HashMap;
import java.util.Map;

public class CachedSshConnector implements SshConnector {

    private Map<String, Map<String, SshDetails>> map = new HashMap<>();

    @Override
    public SshDetails getDetails(String host, String folder) {
        Map<String, SshDetails> servers = map.computeIfAbsent(host, h -> new HashMap<>());
        SshDetails server = servers.computeIfAbsent(folder, f -> new SshDetails());
        return server;
    }

}
