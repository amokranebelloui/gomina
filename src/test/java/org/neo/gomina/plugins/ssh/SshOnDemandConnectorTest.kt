package org.neo.gomina.plugins.ssh

import org.junit.Test
import org.neo.gomina.persistence.model.FileInventory
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.integration.ssh.SshClient
import java.io.File

class SshOnDemandConnectorTest {

    @Test
    fun testAnalyze() {
        val inventory = FileInventory("data")
        val sshConfig = SshConfig(listOf(Host("localhost", "Amokrane", "@amokrane")))
        val passwords = Passwords(File("config/pass.properties"))
        val sshClient = SshClient()
        val sshConnector = SshOnDemandConnector(sshConfig!!)
        sshConnector.inventory = inventory
        sshConnector.passwords = passwords
        sshConnector.sshClient = sshClient

        inventory.getEnvironment("UAT") ?. let { sshConnector.analyze(it) }
    }

}
