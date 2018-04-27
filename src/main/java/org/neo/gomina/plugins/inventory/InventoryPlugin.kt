package org.neo.gomina.plugins.inventory

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.model.inventory.Environment
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

    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    override fun init() {
        logger.info("Initializing inventory ...")
        for (env in inventory.getEnvironments()) {
            process(env)
        }
        logger.info("Inventory initialized")
    }

    fun reload(envId: String) {
        logger.info("Reload inventory ...")
        inventory.getEnvironment(envId)?.let {
            val ids = process(it)
            instanceDetailRepository.getInstances(envId)
                    .filter { !ids.contains(it.id) }
                    .forEach { it.unexpected = true }
        }
        logger.info("Inventory reloaded")
    }

    private fun process(env: Environment): List<String> {
        val ids = mutableListOf<String>()
        for (service in env.services) {
            for (envInstance in service.instances) {
                val id = env.id + "-" + envInstance.id
                val instance = instanceDetailRepository.getOrCreateInstance(id) {
                    Instance(id = id, env = env.id, type = service.type, service = service.svc, name = envInstance.id)
                }
                instance.applyInventory(service, envInstance)
                ids.add(id)
            }
        }
        return ids
    }

    companion object {
        private val logger = LogManager.getLogger(InventoryPlugin::class.java)
    }
}

