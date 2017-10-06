package org.neo.gomina.model.inventory;

import org.junit.Test;
import org.neo.gomina.model.inventory.file.FileInventory;

import static org.fest.assertions.Assertions.assertThat;

public class FileInventoryTest {

    @Test
    public void getEnvs() throws Exception {
        Inventory inventory = new FileInventory();
        assertThat(inventory.getEnvs()).containsExactly("uat", "prod");
    }

    @Test
    public void getEnvironment() throws Exception {
        Inventory inventory = new FileInventory();
        Environment env = inventory.getEnvironment("uat");
        assertThat(env.name).isEqualTo("tradex-uat");
        assertThat(env.services.size()).isGreaterThan(2);
    }

}