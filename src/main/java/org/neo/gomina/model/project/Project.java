package org.neo.gomina.model.project;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Project {

    public String id;
    public String type;
    public String svnUrl;
    public String maven;
    public String jenkinsJob;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("svnUrl", svnUrl)
                .append("maven", maven)
                .append("jenkinsJob", jenkinsJob)
                .toString();
    }
}
