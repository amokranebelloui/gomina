package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.security.Passwords
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

typealias AnalysisFunction = (instance: InvInstance, sshClient: SshClient, session: Session, prefix: String, sshDetails: SshDetails) -> Unit

class SshOnDemandConnector {

    private val hosts: Map<String, Host>

    //@Inject internal lateinit var inventory: Inventory
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


    fun analyze(env: Environment, analysisFunction: AnalysisFunction): EnvAnalysis {
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

                        analysisFunction(instance, sshClient, session, prefix, sshDetails)
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

    companion object {
        private val logger = LogManager.getLogger(SshOnDemandConnector::class.java)
    }

}