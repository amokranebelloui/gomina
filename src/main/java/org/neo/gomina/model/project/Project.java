package org.neo.gomina.model.project;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Project {

    public String id;
    public String label;
    public String type;
    public String svnRepo;
    public String svnUrl;
    public String maven;
    public String jenkinsJob;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("label", label)
                .append("type", type)
                .append("svnRepo", svnRepo)
                .append("svnUrl", svnUrl)
                .append("maven", maven)
                .append("jenkinsJob", jenkinsJob)
                .toString();
    }
}
