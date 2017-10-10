package org.neo.gomina.api.diagram;

public class Dependency {

    public String from;
    public String to;

    public Dependency(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
