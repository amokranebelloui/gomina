package org.neo.gomina.plugins.ssh.impl

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.model.ssh.SshAuth
import org.neo.gomina.plugins.ssh.Host
import org.neo.gomina.plugins.ssh.SshConfig
import org.neo.gomina.plugins.ssh.connector.SshClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class SshDetails {
    var analyzed: Boolean = false
    var deployedVersion: String? = null
    var deployedRevision: String? = null
    var confCommitted: Boolean? = null
    var confUpToDate: Boolean? = null
}

class SshOnDemandConnector {

    private val map = ConcurrentHashMap<String, MutableMap<String, SshDetails>>()
    private val hosts: Map<String, Host>

    @Inject internal lateinit var inventory: Inventory
    @Inject internal lateinit var passwords: Passwords
    @Inject internal lateinit var sshClient: SshClient

    @Inject
    constructor(sshConfig: SshConfig) {
        hosts = sshConfig.hosts
        .groupBy { it.host }
        .entries
        .map { (host, configs) -> Pair(host, configs.first())}
        .toMap()
    }

    fun analyze() {
        val instancesByHost: Map<String, List<InvInstance>> = inventory.getEnvironments()
                .flatMap { it.services }
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() && !it.folder.isNullOrBlank() }
                .groupBy { it.host!! }

        for ((host, instances) in instancesByHost) {
            val config = hosts[host]
            if (config != null) {
                val username = config.username
                val password = passwords.getRealPassword(config.passwordAlias)
                val sudo = config.sudo
                try {
                    logger.info("Analyze instances ${instances.size} on '$host' using $username/***${StringUtils.length(password)} $sudo")

                    val auth = SshAuth(username, password, sudo)
                    val session = sshClient.getSession(host, auth)
                    session.connect(1000)
                    val prefix = if (StringUtils.isNotBlank(sudo)) "sudo -u " + sudo else ""

                    for (instance in instances) {
                        val servers = map.getOrPut(host) { ConcurrentHashMap() }
                        val sshDetails = servers.getOrPut(instance.folder!!) { SshDetails() }

                        sshDetails.analyzed = true
                        sshDetails.deployedVersion = deployedVersion(session, instance.folder, prefix)
                        sshDetails.deployedRevision = null
                        sshDetails.confCommitted = checkConfCommited(session, instance.folder, prefix)
                        sshDetails.confUpToDate = null
                        logger.info("Analyzed $host ${instance.folder} $sshDetails")
                    }
                    session.disconnect()
                }
                catch (e: Exception) {
                    logger.error("", e)
                }

            } else {
                logger.warn("No SSH config for '{}'", host)
            }
        }
    }

    fun checkConfCommited(session: Session, applicationFolder: String?, prefix: String): Boolean? {
        val result = sshClient.executeCommand(session, "$prefix svn status $applicationFolder/config")
        return if (StringUtils.isBlank(result)) java.lang.Boolean.TRUE else if (result.contains("is not a working copy")) null else java.lang.Boolean.FALSE
    }

    fun deployedVersion(session: Session, applicationFolder: String?, prefix: String): String {
        var result = sshClient.executeCommand(session, "$prefix cat $applicationFolder/current/version.txt 2>/dev/null")
        result = StringUtils.trim(result)
        if (StringUtils.isBlank(result)) {
            result = sshClient.executeCommand(session, "$prefix ls -ll $applicationFolder/current")
            val pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/"
            result = result.replace(pattern.toRegex(), "$1").trim { it <= ' ' }
        }
        return result
    }

    fun getDetails(host: String?, folder: String?): SshDetails {
        var sshDetails: SshDetails? = null
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(folder)) {
            val servers = map[host]
            if (servers != null) {
                sshDetails = servers[folder]
            }
        }
        return if (sshDetails != null) sshDetails else SshDetails()
    }

    companion object {
        private val logger = LogManager.getLogger(SshOnDemandConnector::class.java)
    }

}