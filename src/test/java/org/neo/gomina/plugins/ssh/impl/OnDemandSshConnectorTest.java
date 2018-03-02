package org.neo.gomina.plugins.ssh.impl;

import org.junit.Test;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;
import org.neo.gomina.model.security.Passwords;
import org.neo.gomina.plugins.ssh.connector.SshClient;
import org.neo.gomina.plugins.ssh.SshConfig;
import org.neo.gomina.module.config.ConfigLoader;

import java.io.File;

public class OnDemandSshConnectorTest {

    @Test
    public void testAnalyze() throws Exception {
        Inventory inventory = new FileInventory("data");
        SshConfig sshConfig = new ConfigLoader().load().ssh;
        Passwords passwords = new Passwords(new File("config/pass.properties"));
        SshClient sshClient = new SshClient();
        OnDemandSshConnector sshConnector = new OnDemandSshConnector(inventory, sshConfig, passwords, sshClient);

        sshConnector.analyze();

    }
}
