package org.neo.gomina.plugins.inventory

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.plugins.Plugin
import javax.inject.Inject

fun Instance.applyInventory(service: Service, envInstance: InvInstance) {
    this.unexpected = false
    this.type = service.type
    this.service = service.svc
    this.project = service.project
    this.deployHost = envInstance.host
    this.deployFolder = envInstance.folder
}

class InventoryPlugin : Plugin {

    @Inject private lateinit var inventory: Inventory

    fun reload(envId: String) {
        logger.info("Reload inventory")
    }

    companion object {
        private val logger = LogManager.getLogger(InventoryPlugin::class.java)
    }
}

