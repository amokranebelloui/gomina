package org.neo.gomina.integration.ssh

import java.util.*


fun main(args: Array<String>) {
    val sshClient = SshClient()

    print("Password:")
    val `in` = Scanner(System.`in`)

    val sshAuth = SshAuth("Test", `in`.next(), "Test")
    val session = sshClient.getSession("localhost", sshAuth)
    //sshClient.whoami(session);
    session.connect(1000)
    println(sshClient.executeCommand(session, "whoami"))
    println(sshClient.executeCommand(session, "whoami"))
    println(sshClient.executeCommand(session, "who am i"))

    session.disconnect()
}