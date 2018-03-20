package org.neo.gomina.model.inventory

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.neo.gomina.model.inventory.file.FileInventory
import java.io.File

class FileInventoryTest {

    @Test
    @Throws(Exception::class)
    fun getEnvs() {
        val inventory = FileInventory("data")
        val environments = inventory.getEnvironments()
        println(environments)
        assertThat(environments).onProperty("id").containsOnly("PROD", "UAT", "DEV")
    }

    @Test
    @Throws(Exception::class)
    fun getEnvironment() {
        val inventory = FileInventory("data")
        val env = inventory.getEnvironment("UAT")
        println(env)
        assertThat(env!!.name).isEqualTo("tradex-uat")
        assertThat(env!!.services.size).isGreaterThan(2)
    }

    @Test
    fun testReadOldModel() {
        val inventory = FileInventory("data")
        //Environment environment = inventory.readEnv(new File("env.monitorx-stg.json"));
        val environment = inventory.readEnv(File("env.tradex-uat.json"))
        println(environment)
    }
}