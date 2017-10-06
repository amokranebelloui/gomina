package org.neo.gomina.model.inventory;

import java.util.List;

public interface Inventory {
    List<String> getEnvs();

    Environment getEnvironment(String envName);
}
