package org.neo.gomina.model.sshinfo.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.ssh.SshAuth;
import org.neo.gomina.model.ssh.SshClient;
import org.neo.gomina.model.sshinfo.SshConfig;
import org.neo.gomina.model.sshinfo.SshConnector;
import org.neo.gomina.model.sshinfo.SshDetails;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OnDemandSshConnector implements SshConnector {

    private final static Logger logger = LogManager.getLogger(OnDemandSshConnector.class);

    private Map<String, Map<String, SshDetails>> map = new ConcurrentHashMap<>();

    private Inventory inventory;
    private Map<String, SshConfig.Host> hosts;
    private Passwords passwords;
    private SshClient sshClient;

    @Inject
    public OnDemandSshConnector(Inventory inventory, SshConfig sshConfig, Passwords passwords, SshClient sshClient) {
        this.inventory = inventory;
        hosts = sshConfig.hosts.stream()
                .collect(Collectors.toMap(SshConfig.Host::getHost, Function.identity()));
        this.passwords = passwords;
        this.sshClient = sshClient;
    }

    @Override
    public void analyze() {
        Map<String, List<InvInstance>> instancesByHost = inventory.getEnvironments().stream()
                .flatMap(env -> env.services.stream())
                .flatMap(svc -> svc.instances.stream()
                        .filter(this::valid)
                )
                .collect(Collectors.groupingBy(ins -> ins.host, Collectors.toList()));

        for (Map.Entry<String, List<InvInstance>> entry : instancesByHost.entrySet()) {
            String host = entry.getKey();
            SshConfig.Host config = hosts.get(host);
            if (config != null) {
                String username = config.username;
                String password = passwords.getRealPassword(config.passwordAlias);
                String sudo = config.sudo;
                try {
                    SshAuth auth = new SshAuth(username, password, sudo);
                    ///Session session = sshClient.getSession(host, auth);
                    ///session.connect(1000);

                    List<InvInstance> instances = entry.getValue();
                    logger.info("Analyze instances {} on '{}' using {}/***{} {}",
                            instances.size(), host, username, StringUtils.length(password), sudo);

                    for (InvInstance instance : instances) {
                        // FIXME get info
                        Random random = new Random();
                        Boolean commited = random.nextBoolean() ? (random.nextBoolean() ? null : Boolean.FALSE) : Boolean.TRUE;
                        SshDetails sshDetails = getOrCreate(host, instance.folder);
                        sshDetails.analyzed = true;
                        //sshDetails.deployedVersion = ;
                        //sshDetails.deployedRevision = ;
                        sshDetails.confCommitted = commited;
                        //sshDetails.confUpToDate = ;
                        logger.info("Analyzed {} {} {}", host, instance.folder, sshDetails);

                        ///sshClient.executeCommand(session, "whoami");
                    }
                    ///session.disconnect();
                }
                catch (Exception e) {
                    logger.error("", e);
                }
            }
            else {
                logger.warn("No SSH config for '{}'", host);
            }
        }
    }

    private boolean valid(InvInstance ins) {
        return StringUtils.isNotBlank(ins.host) && StringUtils.isNotBlank(ins.folder);
    }

    public SshDetails getOrCreate(String host, String folder) {
        Map<String, SshDetails> servers = map.computeIfAbsent(host, h -> new ConcurrentHashMap<>());
        return servers.computeIfAbsent(folder, f -> new SshDetails());
    }

    @Override
    public SshDetails getDetails(String host, String folder) {
        SshDetails sshDetails = null;
        if (StringUtils.isNotBlank(host)&& StringUtils.isNotBlank(folder)) {
            Map<String, SshDetails> servers = map.get(host);
            if (servers != null) {
                sshDetails = servers.get(folder);
            }
        }
        return sshDetails != null ? sshDetails : new SshDetails();
    }

}
