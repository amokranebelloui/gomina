package org.neo.gomina.model.monitoring.zmq;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class ZmqMonitorConfig {

    public List<Connection> connections;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("connections", connections)
                .toString();
    }

    public static class Connection {

        public String url;
        //public String type;
        //public String subscriptions;
        //public Class<?> serializationClass;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("url", url)
                    //.append("type", type)
                    //.append("subscriptions", subscriptions)
                    //.append("serializationClass", serializationClass)
                    .toString();
        }
    }
}
