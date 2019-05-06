package org.neo.gomina.integration.ssh

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.security.Passwords
import javax.inject.Inject

class DummyHostConnector {

    @Inject internal lateinit var hosts: Hosts
    @Inject internal lateinit var passwords: Passwords
    @Inject internal lateinit var sshClient: SshClient

    fun <T> process(host: Host, function: () -> T?): T? {
        return function.invoke()
    }

    companion object {
        private val logger = LogManager.getLogger(DummyHostConnector::class.java)
    }

}