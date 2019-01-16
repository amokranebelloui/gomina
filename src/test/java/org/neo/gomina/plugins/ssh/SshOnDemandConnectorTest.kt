package org.neo.gomina.plugins.ssh

import com.jcraft.jsch.ChannelExec
import org.apache.logging.log4j.LogManager
import org.junit.Test
import org.neo.gomina.integration.ssh.SshClient
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.integration.ssh.execute
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.persistence.model.InventoryFile
import java.io.File

class SshOnDemandConnectorTest {

    private val logger = LogManager.getLogger(SshOnDemandConnectorTest::class.java)

    class DummyHosts : Hosts {
        val host1 = Host("localhost", "paris", "Test", "test", username = "@test")
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
            sshConnector.analyze(it) { session, sudo, instances ->
                instances.map { instance ->
                    instance.folder!! to InstanceSshDetails(
                            analyzed = true,
                            deployedVersion = session.execute("whoami")
                    )
                }
                .toMap()
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

            val channel = session.openChannel("exec") as ChannelExec
            channel.setPty(true)

            data class Instance(val id: String, val folder: String)
            val instances = listOf(
                    Instance("lp", "/srv/ed/apps/tradex-uat-liquidityprovider/lp"),
                    Instance("market", "/srv/ed/apps/tradex-uat-marketmanager/market"),
                    Instance("unknown", "/srv/ed/apps/tradex-uat-unknown/unknown")
            )

            val flatMap = instances.flatMap {
                listOf(
                        "echo \"${it.id}.version=`cat ${it.folder}/current/version.txt 2>/dev/null`\"",
                        //"echo \"${it.id}.version=`cat ${it.folder}/current/version.properties 2>/dev/null | grep version`\"",
                        "echo \"${it.id}.revision=`cat ${it.folder}/current/version.properties 2>/dev/null | grep revision | cut -c14-`\"",
                        //"echo \"${it.id}.version2=`ls -ll $it.folder/current/libs`\"",
                        "echo \"${it.id}.conf.revision=`svn info ${it.folder}/config 2>/dev/null | grep Revision: |cut -c11-`\"",
                        "echo \"${it.id}.conf.status=$(echo \"\$(result=\$(svn status ${it.folder}/config 2>&1); notvers=\$(echo \$result | grep 'is not a working copy'); if [ ! -z \"\$notvers\" ]; then echo 'not versioned'; elif [ ! -z \"\$result\" ]; then echo 'modified'; else echo 'ok' ; fi)\")\""
                        //"echo \"${it.id}.conf.status=$(svn status ${it.folder}/config)\""
                )
            }

            val res = session.execute(flatMap)
                    .lines()
                    .filter { it.contains('=') }
                    .map { it.split('=', limit = 2) }
                    .map { it[0] to it[1] }
                    .toMap()

            logger.info("Res:")
            res.forEach { (k, v) ->
                logger.info("   $k = $v")
            }
            res
        }
    }
}
