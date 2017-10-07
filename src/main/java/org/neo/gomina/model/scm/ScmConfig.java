package org.neo.gomina.model.scm;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class ScmConfig {

    public List<ScmRepo> repos;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("repos", repos)
                .toString();
    }

    public static class ScmRepo {

        public String id;
        public String type;
        public String location;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", id)
                    .append("type", type)
                    .append("location", location)
                    .toString();
        }
    }

}
