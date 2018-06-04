package org.neo.gomina.plugins.ssh

import org.junit.Test
import org.neo.gomina.integration.ssh.SshClient
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.integration.ssh.execute
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.persistence.model.InventoryFile
import java.io.File

class SshOnDemandConnectorTest {

    class DummyHosts : Hosts {
        val host1 = Host("localhost", "paris", "Test", "@test")
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
            sshConnector.analyze(it) { instance, session, sudo ->
                SshDetails(
                    analyzed = true,
                    deployedVersion = session.execute("whoami")
                )
            }
        }
    }

    @Test
    fun testAnalyzeHost() {
        val passwords = Passwords(File("config/pass.properties"))
        val sshClient = SshClient()
        val sshConnector = SshOnDemandConnector()
        sshConnector.hosts = DummyHosts()
        sshConnector.passwords = passwords
        sshConnector.sshClient = sshClient

        sshConnector.analyze("localhost") { session, sudo ->
            val result = session.sudo(sudo, "find /Users/Test/Work -mindepth 1 -maxdepth 1 -type d")
            val list = when {
                result.contains("No such file or directory") -> emptyList()
                else -> result.split("\n").filter { it.isNotBlank() }.map { it.trim() }
            }
            println(result)
            println(list)
        }
    }
}
