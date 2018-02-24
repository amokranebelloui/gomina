package org.neo.gomina.model.inventory;

import org.junit.Test;
import org.neo.gomina.model.inventory.file.FileInventory;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class FileInventoryTest {

    @Test
    public void getEnvs() throws Exception {
        Inventory inventory = new FileInventory("data");
        Collection<Environment> environments = inventory.getEnvironments();
        System.out.println(environments);
        assertThat(environments).onProperty("id").containsOnly("PROD", "UAT", "DEV");
    }

    @Test
    public void getEnvironment() throws Exception {
        Inventory inventory = new FileInventory("data");
        Environment env = inventory.getEnvironment("UAT");
        System.out.println(env);
        assertThat(env.getName()).isEqualTo("tradex-uat");
        assertThat(env.getServices().size()).isGreaterThan(2);
    }

    @Test
    public void testReadOldModel() {
        FileInventory inventory = new FileInventory("data");
        //Environment environment = inventory.readEnv(new File("env.monitorx-stg.json"));
        Environment environment = inventory.readEnv(new File("env.tradex-uat.json"));
        System.out.println(environment);
    }
}