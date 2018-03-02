package org.neo.gomina.plugins.ssh.connector;

import com.jcraft.jsch.Session;
import org.neo.gomina.model.ssh.SshAuth;

import java.util.Scanner;

public class SshClientTest {

    public static void main(String[] args) throws Exception {
        SshClient sshClient = new SshClient();

        System.out.print("Password:");
        Scanner in = new Scanner(System.in);

        SshAuth sshAuth = new SshAuth("Amokrane", in.next(), "Amokrane");
        Session session = sshClient.getSession("Amokranes-MacBook-Pro.local", sshAuth);
        //sshClient.whoami(session);
        session.connect(1000);
        sshClient.executeCommand(session, "whoami");
        sshClient.executeCommand(session, "whoami");
        sshClient.executeCommand(session, "whoami");

        //System.out.println("Res " + sshClient.deployedVersion(session, "/srv/ed/apps/tradex-uat-pita/pita", "sudo -u Amokrane"));

        session.disconnect();
    }

}