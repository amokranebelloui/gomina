package org.neo.gomina.model.inventory;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class Environment {

    public String name;
    public String code;
    public String type;
    public String monitoringUrl;
    public boolean active;
    public List<Service> services;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("code", code)
                .append("type", type)
                .append("monitoringUrl", monitoringUrl)
                .append("active", active)
                .append("services", services)
                .toString();
    }
}
