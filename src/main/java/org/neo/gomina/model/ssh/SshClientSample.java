package org.neo.gomina.model.ssh;

import com.jcraft.jsch.Session;

public class SshClientSample {

    public static void main(String[] args) throws Exception {
        SshClient sshClient = new SshClient();

        SshAuthentication sshAuthentication = new SshAuthentication();
        SshAuth sshAuth = sshAuthentication.get("Amokranes-MacBook-Pro.local");
        Session session = sshClient.getSession("Amokranes-MacBook-Pro.local", sshAuth);
        sshClient.test(session);
    }

}
