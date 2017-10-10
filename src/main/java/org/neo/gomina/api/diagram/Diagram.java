package org.neo.gomina.api.diagram;

import java.util.List;

public class Diagram {

    public List<Component> components;
    public List<Dependency> dependencies;

    public Diagram(List<Component> components, List<Dependency> dependencies) {
        this.components = components;
        this.dependencies = dependencies;
    }
}
