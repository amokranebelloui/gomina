package org.neo.gomina.plugins.ssh

import org.junit.Test
import org.neo.gomina.integration.ssh.SshClient
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.persistence.model.InventoryFile
import java.io.File

class SshOnDemandConnectorTest {

    class DummyHosts : Hosts {
        val host1 = Host("localhost", "Amokrane", "@amokrane")
        override fun getHosts(): List<Host> = listOf(host1)
        override fun getHost(host: String): Host? = host1
    }

    @Test
    fun testAnalyze() {
        val inventory = InventoryFile("data")
        val passwords = Passwords(File("config/pass.properties"))
        val sshClient = SshClient()
        val sshConnector = SshOnDemandConnector()
        sshConnector.hosts = DummyHosts()
        sshConnector.passwords = passwords
        sshConnector.sshClient = sshClient

        inventory.getEnvironment("UAT") ?. let {
            sshConnector.analyze(it) { instance, sshClient, session, prefix, sshDetails ->
                sshDetails.analyzed = true
                sshDetails.deployedVersion = sshClient.executeCommand(session, "whoami")
            }
        }
    }

}
