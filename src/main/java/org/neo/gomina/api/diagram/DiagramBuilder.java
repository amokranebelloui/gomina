package org.neo.gomina.api.diagram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagramBuilder {

    private final static Logger logger = LogManager.getLogger(DiagramBuilder.class);

    private Map<String, Component> components = new HashMap<>();
    private List<Dependency> dependencies = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private File file = new File("data/architecture.json");

    public DiagramBuilder() {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        try {
            Diagram diagram = objectMapper.readValue(file, Diagram.class);
            for (Component component : diagram.components) {
                addComponent(component);
            }
            for (Dependency dependency : diagram.dependencies) {
                dependencies.add(dependency);
            }
        }
        catch (IOException e) {
            logger.error("Error reading diagram", e);
        }
    }

    private void addComponent(Component component) {
        components.put(component.name, component);
    }

    public void updateComponent(String name, int x, int y) {
        Component component = components.get(name);
        component.x = x;
        component.y = y;

        try {
            objectMapper.writeValue(file, getDiagram());
        }
        catch (IOException e) {
            logger.error("Error writing diagram", e);
        }
    }

    public Diagram getDiagram() {
        return new Diagram(new ArrayList(components.values()), dependencies);
    }

}
