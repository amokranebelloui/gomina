package org.neo.gomina.model.inventory;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Instance {

    public String id;
    public String host;
    public String folder;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("host", host)
                .append("folder", folder)
                .toString();
    }
}
