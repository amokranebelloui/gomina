package org.neo.gomina.api.diagram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DiagramBuilder {

    private Map<String, Component> components = new HashMap<>();

    public DiagramBuilder() {
                addComponent(new Component("referential", 400, 350));
                addComponent(new Component("basket", 100, 200));
                addComponent(new Component("order", 150, 120));
                addComponent(new Component("market", 200, 200));
                addComponent(new Component("cross", 300, 100));
                addComponent(new Component("fixout", 300, 150));
                addComponent(new Component("emma", 300, 200));
                addComponent(new Component("fidessa", 150, 50));
                addComponent(new Component("pie", 100, 50));
                addComponent(new Component("pita", 50, 50));
                addComponent(new Component("fixin", 50, 150));
                addComponent(new Component("audit", 150, 350));
                addComponent(new Component("mis", 200, 350));
    }

    private void addComponent(Component component) {
        components.put(component.name, component);
    }

    public void updateComponent(String name, int x, int y) {
        Component component = components.get(name);
        component.x = x;
        component.y = y;
    }

    public Diagram getDiagram() {
        return new Diagram(
                new ArrayList(components.values()),
                Arrays.asList(
                        new Dependency("basket", "order"),
                        new Dependency("market", "cross"),
                        new Dependency("market", "fixout"),
                        new Dependency("market", "emma"),
                        new Dependency("market", "order"),
                        new Dependency("audit", "mis")
                )
        );
    }

}
