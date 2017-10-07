package org.neo.gomina.model.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public Boolean checkConfCommited(Session session, String applicationFolder, String prefix) {
        if (StringUtils.isNotBlank(applicationFolder)) {
            try {
                String cmd = prefix + " svn status " + applicationFolder + "/config";
                logger.info(session.getHost() + ": " + cmd);
                String result = executeCommand(session, cmd);
                logger.info(result);
                return StringUtils.isBlank(result) ? Boolean.TRUE : (result.contains("is not a working copy") ? null : Boolean.FALSE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String deployedVersion(Session session, String applicationFolder, String prefix) {
        if (StringUtils.isNotBlank(applicationFolder)) {
            try {
                //logger.info(session.getHost() + ": " + cmd);
                String result = executeCommand(session, prefix + " cat " + applicationFolder + "/current/version.txt 2>/dev/null");
                result = StringUtils.trim(result);
                if (StringUtils.isBlank(result)) {
                    result = executeCommand(session, prefix + " ls -ll " + applicationFolder + "/current");
                    String pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/";
                    result = result.replaceAll(pattern, "$1").trim();
                }
                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
        System.out.println("#CMD: " + cmd);
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
        System.out.println("#RES: " + res + " in(" + (System.nanoTime() - start) + ")");
        return res;
    }

}
