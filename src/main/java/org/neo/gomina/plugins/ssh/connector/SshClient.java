package org.neo.gomina.plugins.ssh.connector;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.ssh.SshAuth;

import java.io.ByteArrayOutputStream;

public class SshClient {

    private static final Logger logger = LogManager.getLogger(SshClient.class);

    private JSch jsch = new JSch();
    private int connectTimeout = 3000;

    public Session getSession(String host, SshAuth auth) throws JSchException {
        Session session = jsch.getSession(auth.getUsername(), host, 22);
        if (StringUtils.isNotBlank(auth.getPassword())) {
            session.setPassword(auth.getPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    public void deploy(Session session, String applicationFolder, String version) throws Exception {
        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/release.sh " + version;
        String result = executeCommand(session, cmd);
    }

    public void run(Session session, String applicationFolder) throws Exception {
        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/run-all.sh";
        String result = executeCommand(session, cmd);
    }

    public void stop(Session session, String applicationFolder) throws Exception {
        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/stop-all.sh";
        String result = executeCommand(session, cmd);
    }

    public String whoami(Session session) throws Exception {
        return executeCommand(session, "whoami");
    }

    public String executeCommand(Session session, String cmd) throws Exception {
        logger.info("#CMD[" + session.getHost() + "]: " + cmd);
        long start = System.nanoTime();
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setPty(true);
        channel.setCommand(cmd);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channel.setOutputStream(baos);
        channel.connect();
        while (!channel.isClosed()) {
            Thread.sleep(2);
        }
        String res = new String(baos.toByteArray());
        logger.info("#RES: " + res + " in(" + (System.nanoTime() - start) + ")");
        return res;
    }

}
