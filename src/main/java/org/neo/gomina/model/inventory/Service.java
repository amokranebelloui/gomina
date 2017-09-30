package org.neo.gomina.model.inventory;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class Service {

    public String svc;
    public String type;
    public String project;
    public List<InvInstance> instances;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("svc", svc)
                .append("type", type)
                .append("project", project)
                .append("instances", instances)
                .toString();
    }
}
