package org.neo.gomina.model.monitoring.zmq;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ZmqConnection {

    public String type;
    public String url;
    public String subscriptions;
    public Class<?> serializationClass;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", type)
                .append("url", url)
                .append("subscriptions", subscriptions)
                .append("serializationClass", serializationClass)
                .toString();
    }
}
