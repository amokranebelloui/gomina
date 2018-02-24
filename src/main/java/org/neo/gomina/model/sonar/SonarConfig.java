package org.neo.gomina.model.sonar;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SonarConfig {

    public String mode;
    public String url;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("mode", mode)
                .append("url", url)
                .toString();
    }
}
