package org.neo.gomina.model.inventory;

import java.util.List;

public interface Inventory {

    /**
     * Get envs
     */
    List<Environment> getEnvironments();

    /**
     * Get env for particular id
     */
    Environment getEnvironment(String env);
}
