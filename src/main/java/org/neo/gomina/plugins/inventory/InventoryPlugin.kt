package org.neo.gomina.plugins.inventory

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import javax.inject.Inject

fun Instance.applyInventory(service: Service, envInstance: InvInstance) {
    this.project = service.project
    this.deployHost = envInstance.host
    this.deployFolder = envInstance.folder
}

class InventoryPlugin : InstancesExt {

    @Inject private lateinit var inventory: Inventory

    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    override fun instancesInit() {
        logger.info("Initializing instances ...")
        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = Instance(id=id, env=env.id, type=service.type, service=service.svc, name=envInstance.id)
                    instance.applyInventory(service, envInstance)
                    instanceDetailRepository.addInstance(id, instance)
                }
            }
        }
        logger.info("Instances initialized")
    }

    companion object {
        private val logger = LogManager.getLogger(InventoryPlugin::class.java)
    }
}

