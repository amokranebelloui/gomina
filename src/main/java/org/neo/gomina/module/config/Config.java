package org.neo.gomina.module.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo.gomina.model.monitoring.zmq.ZmqMonitorConfig;
import org.neo.gomina.model.scm.ScmConfig;
import org.neo.gomina.model.sonar.SonarConfig;
import org.neo.gomina.model.sshinfo.SshConfig;

import java.util.Map;

public class Config {

    public String name;
    public String passwordsFile;

    public Map<String, String> inventory;

    public ScmConfig scm;
    public SonarConfig sonar;
    public SshConfig ssh;

    public ZmqMonitorConfig zmqMonitoring;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("passwordsFile", passwordsFile)
                .append("inventory", inventory)
                .append("scm", scm)
                .append("sonar", sonar)
                .append("ssh", ssh)
                .append("zmqMonitoring", zmqMonitoring)
                .toString();
    }
}
