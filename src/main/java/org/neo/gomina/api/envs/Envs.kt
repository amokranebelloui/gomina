package org.neo.gomina.api.envs

import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

/**
 * type: PROD, TEST
 */
data class Env(val env: String, val type:String, val app:String)

class EnvBuilder {
    @Inject private lateinit var inventory: Inventory
    fun getEnvs(): Collection<Env> {
        return inventory.getEnvironments().map {
            Env(it.id, it.type, "myproject") // FIXME project
        }
    }
}