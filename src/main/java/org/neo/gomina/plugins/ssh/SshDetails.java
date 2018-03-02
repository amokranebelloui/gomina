package org.neo.gomina.plugins.ssh;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SshDetails {

    public boolean analyzed;
    public String deployedVersion;
    public String deployedRevision;
    public Boolean confCommitted;
    public Boolean confUpToDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("analyzed", analyzed)
                .append("deployedVersion", deployedVersion)
                .append("deployedRevision", deployedRevision)
                .append("confCommitted", confCommitted)
                .append("confUpToDate", confUpToDate)
                .toString();
    }
}
