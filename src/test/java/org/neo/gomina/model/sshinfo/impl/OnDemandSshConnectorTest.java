package org.neo.gomina.model.sshinfo.impl;

import org.junit.Test;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.model.ssh.SshClient;
import org.neo.gomina.model.sshinfo.SshConfig;
import org.neo.gomina.runner.config.ConfigLoader;

public class OnDemandSshConnectorTest {

    @Test
    public void testAnalyze() throws Exception {
        Inventory inventory = new FileInventory();
        SshConfig sshConfig = new ConfigLoader().load().ssh;
        Passwords passwords = new Passwords();
        SshClient sshClient = new SshClient();
        OnDemandSshConnector sshConnector = new OnDemandSshConnector(inventory, sshConfig, passwords, sshClient);

        sshConnector.analyze();

    }
}