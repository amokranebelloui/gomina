package org.neo.gomina.model.ssh;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class SshClient {

    private static final Logger logger = LogManager.getLogger(SshClient.class);

    private JSch jsch;
    private SshAuthentication authentication = new SshAuthentication();
    private int connectTimeout = 30000;

    public SshClient() {
        jsch = new JSch();
    }
/*
    public static void main(String[] args) throws Exception {
        SshClient client = new SshClient();
        Session session = client.getSession("vil-trdex-901");
        session.connect(30000);
        logger.info("Res " + client.deployedVersion(session, "/srv/ed/apps/tradex-uat-pita/pita", "sudo -u svc-ed-int"));
        session.disconnect();
    }
    */

    public Session getSession(String host, SshAuth auth) throws JSchException {
        Session session = jsch.getSession(auth.getUsername(), host, 22);
        session.setPassword(auth.getPassword());
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
        //Session session = getSession(host);
        session.connect(connectTimeout);

        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/release.sh " + version;
        String result = executeCommand(session, cmd);
        logResult(cmd, result);

        session.disconnect();
    }

    public void run(Session session, String applicationFolder) throws Exception {
        //Session session = getSession(host);
        session.connect(connectTimeout);

        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/run-all.sh";
        String result = executeCommand(session, cmd);
        logResult(cmd, result);

        session.disconnect();
    }

    public void stop(Session session, String applicationFolder) throws Exception {
        //Session session = getSession(host);
        session.connect(connectTimeout);

        String cmd = "sudo -u svc-ed-int /srv/ed/apps/" + applicationFolder + "/ops/stop-all.sh";
        String result = executeCommand(session, cmd);
        logResult(cmd, result);

        session.disconnect();
    }

    public void test(Session session) throws Exception {
        //Session session = getSession(host);
        session.connect(connectTimeout);

        String cmd1 = "whoami";
        String result1 = executeCommand(session, cmd1);
        logResult(cmd1, result1);
        //System.out.println("Result:\n" + executeCommand(session, "sudo -s -u svc-ed-int"));
        String cmd2 = "sudo -u Amokrane whoami";
        String result2 = executeCommand(session, cmd2);
        logResult(cmd2, result2);
        //System.out.println("Result:\n" + executeCommand(session, "sudo -u svc-ed-int cd /srv/ed/apps/" + applicationFolder));
        //System.out.println("Result:\n" + executeCommand(session, "sudo -u svc-ed-int ls"));

        session.disconnect();
    }

    private static String executeCommand(Session session, String cmd) throws Exception {
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
        System.out.println("Exe cmd " + (System.nanoTime() - start));
        return res;
    }

    @Deprecated // Doesn work
    private static String executeShell(Session session, String cmd) throws Exception {
        long start = System.nanoTime();
        ChannelShell channel = (ChannelShell)session.openChannel("shell");
        channel.setPty(true);
        OutputStream ops = channel.getOutputStream();
        PrintStream ps = new PrintStream(ops, true);

        //channel.setCommand(cmd);
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //channel.setOutputStream(baos);
        channel.connect();
        ps.println(cmd);

        InputStream in = channel.getInputStream();
        String theString = IOUtils.toString(in, Charset.forName("UTF-8"));
        System.out.println("*" + theString);

        while (!channel.isClosed()) {
            Thread.sleep(2);
        }
        //String res = new String(baos.toByteArray());
        System.out.println("Exe cmd " + (System.nanoTime() - start));
        return "res";
    }

    private static void logResult(String cmd, String result) {
        System.out.println("----- Command:\n" + cmd);
        System.out.println("----- Result :\n" + result);
    }

}
