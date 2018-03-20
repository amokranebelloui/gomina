package org.neo.gomina.plugins.ssh.connector

import org.neo.gomina.model.ssh.SshAuth
import java.util.*


object SshClientTest {

    fun main(args: Array<String>) {
        val sshClient = SshClient()

        print("Password:")
        val `in` = Scanner(System.`in`)

        val sshAuth = SshAuth("Amokrane", `in`.next(), "Amokrane")
        val session = sshClient.getSession("Amokranes-MacBook-Pro.local", sshAuth)
        //sshClient.whoami(session);
        session.connect(1000)
        sshClient.executeCommand(session, "whoami")
        sshClient.executeCommand(session, "whoami")
        sshClient.executeCommand(session, "whoami")

        //System.out.println("Res " + sshClient.deployedVersion(session, "/srv/ed/apps/tradex-uat-pita/pita", "sudo -u Amokrane"));

        session.disconnect()
    }

}