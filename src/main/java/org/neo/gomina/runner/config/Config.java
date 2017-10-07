package org.neo.gomina.runner.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo.gomina.model.monitoring.zmq.ZmqMonitorConfig;
import org.neo.gomina.model.scm.ScmConfig;

public class Config {

    public String name;

    public ScmConfig scm;

    public ZmqMonitorConfig zmqMonitoring;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("scm", scm)
                .append("zmqMonitoring", zmqMonitoring)
                .toString();
    }
}
