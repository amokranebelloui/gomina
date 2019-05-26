package org.neo.gomina.module

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.name.Names
import org.junit.Test
import java.io.File


class GominaModuleTest {
    @Test
    fun testModule() {
        Guice.createInjector(GominaModule(File("config/config.yaml")))

        val module = object : AbstractModule() {

            override fun configure() {
                bind(String::class.java).annotatedWith(Names.named("inventory.dir")).toInstance("data")
                bind(String::class.java).annotatedWith(Names.named("inventory.filter")).toInstance("*")

            }
        }
        Guice.createInjector(module)

    }
}
