package org.neo.gomina.model.monitoring.zmq;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class ZmqConfig {

    public List<ZmqConnection> connections;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("connections", connections)
                .toString();
    }
}
