package org.neo.gomina.model.sshinfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SshDetails {

    public String deployedVersion;
    public Boolean confCommitted;
    public Boolean confUpToDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("deployedVersion", deployedVersion)
                .append("confCommitted", confCommitted)
                .append("confUpToDate", confUpToDate)
                .toString();
    }
}
