package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.security.Passwords
import javax.inject.Inject

class SshOnDemandConnector {

    @Inject internal lateinit var hosts: Hosts
    @Inject internal lateinit var passwords: Passwords
    @Inject internal lateinit var sshClient: SshClient

    fun <T> process(host: Host, function: (session: Session) -> T?): T? {
        val hostname = host.host
        if (host.username != null) {
            try {
                val sudo = host.sudo
                val password = passwords.getRealPassword(host.passwordAlias!!)
                val session = if (host.proxyHost?.isNotBlank() == true) {
                    val username = host.username + (host.proxyUser.takeIf { !it.isNullOrBlank() }?.let { "@$it" } ?: "") + "@$hostname"
                    logger.info("Analyze '$hostname' using $username/***${StringUtils.length(password)} $sudo")
                    sshClient.getSession(host.proxyHost, SshAuth(username, password, sudo))
                }
                else {
                    val username = host.username
                    logger.info("Analyze '$hostname' using $username/***${StringUtils.length(password)} $sudo")
                    sshClient.getSession(hostname, SshAuth(username, password, sudo))
                }
                session.connect(1000)

                val result = function.invoke(session)
                session.disconnect()
                return result;
            }
            catch (e: Exception) {
                logger.error("", e)
            }
        }
        else {
            logger.warn("No SSH config for '{}'", hostname)
        }
        return null
    }

    companion object {
        private val logger = LogManager.getLogger(SshOnDemandConnector::class.java)
    }

}