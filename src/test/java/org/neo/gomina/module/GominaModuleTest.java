package org.neo.gomina.module;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.junit.Test;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;

public class GominaModuleTest {
    @Test
    public void testModule() {
        Guice.createInjector(new GominaModule());

        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("inventory.dir"))
                        .toInstance("data");
                bind(String.class).annotatedWith(Names.named("inventory.filter"))
                        .toInstance("*");
                // FIXME Type
                bind(Inventory.class).to(FileInventory.class).in(Scopes.SINGLETON);

            }
        };
        Guice.createInjector(module);

    }
}