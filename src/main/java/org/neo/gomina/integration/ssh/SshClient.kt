package org.neo.gomina.integration.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream

private val logger = LogManager.getLogger(Session::class.java)

fun Session.sudo(user: String?, cmd: String) = sudo(user, listOf(cmd))

fun Session.sudo(user: String?, cmds: List<String>): String {
    val sudoPrefix = if (user?.isNotBlank() == true) "sudo -u $user " else ""
    return execute(cmds.map { sudoPrefix + it })
}

fun Session.execute(cmd: String) = execute(listOf(cmd))

fun Session.execute(cmds: List<String>): String {
    val cmd = cmds.joinToString(separator = " ; \n")
    logger.debug("#CMD[${this.host}]: $cmd")
    val start = System.nanoTime()
    val channel = this.openChannel("exec") as ChannelExec
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

    companion object {
        private val logger = LogManager.getLogger(SshClient::class.java)
    }

}