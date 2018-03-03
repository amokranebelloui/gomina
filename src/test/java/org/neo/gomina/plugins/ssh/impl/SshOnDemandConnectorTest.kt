package org.neo.gomina.plugins.ssh.impl

import org.junit.Test
import org.neo.gomina.model.inventory.file.FileInventory
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.module.config.ConfigLoader
import org.neo.gomina.plugins.ssh.connector.SshClient
import java.io.File

class SshOnDemandConnectorTest {

    @Test
    fun testAnalyze() {
        val inventory = FileInventory("data")
        val sshConfig = ConfigLoader().load().ssh
        val passwords = Passwords(File("config/pass.properties"))
        val sshClient = SshClient()
        val sshConnector = SshOnDemandConnector(sshConfig)
        sshConnector.inventory = inventory
        sshConnector.passwords = passwords
        sshConnector.sshClient = sshClient

        sshConnector.analyze()
    }

}
