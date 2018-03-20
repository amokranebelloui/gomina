package org.neo.gomina.module

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Scopes
import com.google.inject.name.Names
import org.junit.Test
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.file.FileInventory


class GominaModuleTest {
    @Test
    fun testModule() {
        Guice.createInjector(GominaModule())

        val module = object : AbstractModule() {

            override fun configure() {
                bind(String::class.java).annotatedWith(Names.named("inventory.dir")).toInstance("data")
                bind(String::class.java).annotatedWith(Names.named("inventory.filter")).toInstance("*")
                // FIXME Type
                bind(Inventory::class.java).to(FileInventory::class.java).`in`(Scopes.SINGLETON)

            }
        }
        Guice.createInjector(module)

    }
}
