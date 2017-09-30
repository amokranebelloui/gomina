package org.neo.gomina.model.inventory;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class InventoryRepositoryTest {

    @Test
    public void getEnvs() throws Exception {
        InventoryRepository inventoryRepository = new InventoryRepository();
        assertThat(inventoryRepository.getEnvs()).containsExactly("uat", "prod");
    }

    @Test
    public void getEnvironment() throws Exception {
        InventoryRepository inventoryRepository = new InventoryRepository();
        Environment env = inventoryRepository.getEnvironment("uat");
        assertThat(env.name).isEqualTo("tradex-uat");
        assertThat(env.services.size()).isGreaterThan(2);
    }

}