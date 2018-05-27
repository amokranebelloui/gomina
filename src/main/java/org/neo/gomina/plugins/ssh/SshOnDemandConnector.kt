package org.neo.gomina.plugins.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.security.Passwords
import org.neo.gomina.integration.ssh.SshAuth
import org.neo.gomina.integration.ssh.SshClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

data class SshDetails (
    var analyzed: Boolean = false,
    var deployedVersion: String? = null,
    var deployedRevision: String? = null,
    var confCommitted: Boolean? = null,
    var confUpToDate: Boolean? = null,
    var confRevision: String? = null
)

class EnvAnalysis(val map: ConcurrentHashMap<String, MutableMap<String, SshDetails>>) {
    fun getFor(host: String?, folder: String?): SshDetails {
        var sshDetails: SshDetails? = null
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(folder)) {
            val servers = map[host]
            if (servers != null) {
                sshDetails = servers[folder]
            }
        }
        return if (sshDetails != null) sshDetails else SshDetails()
    }
}

class SshOnDemandConnector {

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

    fun analyze(env: Environment): EnvAnalysis {
        logger.info("SSH Analysis for ${env.id}")
        val instancesByHost = env.services
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() && !it.folder.isNullOrBlank() }
                .groupBy { it.host!! }

        val map = ConcurrentHashMap<String, MutableMap<String, SshDetails>>()
        for ((host, instances) in instancesByHost) {
            val config = hosts[host]
            if (config != null) {
                val username = config.username
                val password = passwords.getRealPassword(config.passwordAlias!!)
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
                        sshDetails.confRevision = confRevision(session, instance.folder, prefix)
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
        return EnvAnalysis(map)
    }

    fun actions(session: Session, applicationFolder: String, version: String) {
        val deploy = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/release.sh $version"
        val run = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/run-all.sh"
        val stop = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/stop-all.sh"
        val whomia = "whoami"
        //val result = executeCommand(session, cmd)
    }
    
    fun checkConfCommited(session: Session, applicationFolder: String?, prefix: String): Boolean? {
        val result = sshClient.executeCommand(session, "$prefix svn status $applicationFolder/config")
        return if (StringUtils.isBlank(result)) java.lang.Boolean.TRUE else if (result.contains("is not a working copy")) null else java.lang.Boolean.FALSE
    }

    fun confRevision(session: Session, applicationFolder: String?, prefix: String): String? {
        val result = sshClient.executeCommand(session, "$prefix svn info $applicationFolder/config | grep Revision: |cut -c11-")
        return when {
            result.contains("does not exist") -> "?"
            result.contains("is not a working copy") -> "!svn"
            else -> result
        }
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

    companion object {
        private val logger = LogManager.getLogger(SshOnDemandConnector::class.java)
    }

}