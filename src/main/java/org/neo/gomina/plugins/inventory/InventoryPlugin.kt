package org.neo.gomina.plugins.inventory

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.Plugin
import javax.inject.Inject


class InventoryPlugin : Plugin {

    @Inject private lateinit var inventory: Inventory

    fun reload(envId: String) {
        logger.info("Reload inventory")
    }

    companion object {
        private val logger = LogManager.getLogger(InventoryPlugin::class.java)
    }
}

