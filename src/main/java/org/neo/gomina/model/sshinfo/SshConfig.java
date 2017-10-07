package org.neo.gomina.model.sshinfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class SshConfig {

    public List<Host> hosts;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("hosts", hosts)
                .toString();
    }

    public static class Host {

        public String host;
        public String username;
        public String passwordAlias;
        public String sudo;

        public String getHost() {
            return host;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("host", host)
                    .append("username", username)
                    .append("passwordAlias", passwordAlias)
                    .append("sudo", sudo)
                    .toString();
        }
    }
}
