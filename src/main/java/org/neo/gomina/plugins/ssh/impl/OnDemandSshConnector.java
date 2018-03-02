package org.neo.gomina.plugins.ssh.impl;

import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.ssh.SshAuth;
import org.neo.gomina.plugins.ssh.connector.SshClient;
import org.neo.gomina.plugins.ssh.SshConfig;
import org.neo.gomina.plugins.ssh.SshConnector;
import org.neo.gomina.plugins.ssh.SshDetails;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
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
                .flatMap(env -> env.getServices().stream())
                .flatMap(svc -> svc.getInstances().stream()
                        .filter(ins -> StringUtils.isNotBlank(ins.getHost()) && StringUtils.isNotBlank(ins.getFolder()))
                )
                .collect(Collectors.groupingBy(ins -> ins.getHost(), Collectors.toList()));

        for (Map.Entry<String, List<InvInstance>> entry : instancesByHost.entrySet()) {
            String host = entry.getKey();
            SshConfig.Host config = hosts.get(host);
            if (config != null) {
                String username = config.username;
                String password = passwords.getRealPassword(config.passwordAlias);
                String sudo = config.sudo;
                try {
                    List<InvInstance> instances = entry.getValue();
                    logger.info("Analyze instances {} on '{}' using {}/***{} {}",
                            instances.size(), host, username, StringUtils.length(password), sudo);

                    SshAuth auth = new SshAuth(username, password, sudo);
                    Session session = sshClient.getSession(host, auth);
                    session.connect(1000);
                    String prefix = StringUtils.isNotBlank(sudo) ? "sudo -u " + sudo : "";

                    for (InvInstance instance : instances) {
                        Map<String, SshDetails> servers = map.computeIfAbsent(host, h -> new ConcurrentHashMap<>());
                        SshDetails sshDetails = servers.computeIfAbsent(instance.getFolder(), f -> new SshDetails());

                        sshDetails.analyzed = true;
                        sshDetails.deployedVersion = deployedVersion(session, instance.getFolder(), prefix);
                        sshDetails.deployedRevision = null;
                        sshDetails.confCommitted = checkConfCommited(session, instance.getFolder(), prefix);
                        sshDetails.confUpToDate = null;
                        logger.info("Analyzed {} {} {}", host, instance.getFolder(), sshDetails);
                    }
                    session.disconnect();
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

    public Boolean checkConfCommited(Session session, String applicationFolder, String prefix) throws Exception {
        String result = sshClient.executeCommand(session, prefix + " svn status " + applicationFolder + "/config");
        return StringUtils.isBlank(result) ? Boolean.TRUE : (result.contains("is not a working copy") ? null : Boolean.FALSE);
    }

    public String deployedVersion(Session session, String applicationFolder, String prefix) throws Exception {
        String result = sshClient.executeCommand(session, prefix + " cat " + applicationFolder + "/current/version.txt 2>/dev/null");
        result = StringUtils.trim(result);
        if (StringUtils.isBlank(result)) {
            result = sshClient.executeCommand(session, prefix + " ls -ll " + applicationFolder + "/current");
            String pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/";
            result = result.replaceAll(pattern, "$1").trim();
        }
        return result;
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
