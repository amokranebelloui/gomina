package org.neo.gomina.model.svn;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SvnDetails {

    public String id;
    public Integer changes;
    public String latest;
    public String released;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("changes", changes)
                .append("latest", latest)
                .append("released", released)
                .toString();
    }
}
