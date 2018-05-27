package org.neo.gomina.integration.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream

class SshClient {

    private val jsch = JSch()
    private val connectTimeout = 3000

    fun getSession(host: String, auth: SshAuth): Session {
        val session = jsch.getSession(auth.username, host, 22)
        if (StringUtils.isNotBlank(auth.password)) {
            session.setPassword(auth.password)
        }
        session.setConfig("StrictHostKeyChecking", "no")
        return session
    }

    fun executeCommand(session: Session, cmd: String): String {
        logger.debug("#CMD[${session.host}]: $cmd")
        val start = System.nanoTime()
        val channel = session.openChannel("exec") as ChannelExec
        channel.setPty(true)
        channel.setCommand(cmd)
        val baos = ByteArrayOutputStream()
        channel.outputStream = baos
        channel.connect()
        while (!channel.isClosed) {
            Thread.sleep(2)
        }
        val res = String(baos.toByteArray())
        logger.debug("#RES: $res in ${(System.nanoTime() - start)} nano")
        return res
    }

    companion object {
        private val logger = LogManager.getLogger(SshClient::class.java)
    }

}