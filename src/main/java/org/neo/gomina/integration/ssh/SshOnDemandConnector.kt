package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.security.Passwords
import javax.inject.Inject

typealias HostAnalysisFunction<T> = (session: Session, sudo: String?) -> T

typealias InstanceAnalysisFunction<T> = (instance: Instance, session: Session, sudo: String?) -> T

fun <T> Map<String, Map<String, T>>.getFor(host: String?, folder: String?): T? {
    var result: T? = null
    if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(folder)) {
        val servers = this[host]
        if (servers != null) {
            result = servers[folder]
        }
    }
    return result
}

class SshOnDemandConnector {

    @Inject internal lateinit var hosts: Hosts
    @Inject internal lateinit var passwords: Passwords
    @Inject internal lateinit var sshClient: SshClient

    fun <T> analyze(env: Environment, analysisFunction: InstanceAnalysisFunction<T>): Map<String, Map<String, T>> {
        logger.info("SSH Analysis for ${env.id}")
        val instancesByHost = env.services
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() && !it.folder.isNullOrBlank() }
                .groupBy { it.host!! }

        val hostMap = mutableMapOf<String, Map<String, T>>()
        for ((host, instances) in instancesByHost) {
            processHost(host) { session, sudo ->
                logger.info("${instances.size} instances to analyze")
                val instanceMap = mutableMapOf<String, T>()
                for (instance in instances) {
                    val t = analysisFunction(instance, session, sudo)
                    instanceMap.put(instance.folder!!, t)
                    logger.info("Analyzed $host ${instance.folder} $t")
                }
                hostMap.put(host, instanceMap)
            }
        }
        logger.info("SSH Analysis for ${env.id} done")
        return hostMap
    }

    fun <T> analyze(host: String, analysisFunction: HostAnalysisFunction<T>): T? {
        logger.info("SSH Analysis for $host")

        var result:T? = null
        processHost(host) { session, sudo ->
            result = analysisFunction.invoke(session, sudo)
        }
        return result
    }

    private fun processHost(host: String, function: (session: Session, sudo: String?) -> Unit) {
        val config = hosts.getHost(host)
        if (config != null) {
            try {
                val username = config.username
                val password = passwords.getRealPassword(config.passwordAlias!!)
                val sudo = config.sudo
                logger.info("Analyze '$host' using $username/***${StringUtils.length(password)} $sudo")
                val auth = SshAuth(username, password, sudo)
                val session = sshClient.getSession(host, auth)
                session.connect(1000)

                function.invoke(session, sudo)
                session.disconnect()
            } catch (e: Exception) {
                logger.error("", e)
            }

        } else {
            logger.warn("No SSH config for '{}'", host)
        }
    }

    companion object {
        private val logger = LogManager.getLogger(SshOnDemandConnector::class.java)
    }

}