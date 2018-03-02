package org.neo.gomina.plugins.inventory

import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import javax.inject.Inject

class InventoryPlugin : InstancesExt {

    @Inject private lateinit var inventory: Inventory

    override fun onGetInstances(envId: String, instances: Instances) {
        val env = inventory.getEnvironment(envId)!!
        for (service in env.services) {
            for (envInstance in service.instances) {
                val id = env.id + "-" + envInstance.id
                var instance = instances.ensure(id, env.id, service.type, service.svc, envInstance.id)
                instance.applyInventory(service, envInstance)
            }
        }
    }

}

fun Instance.applyInventory(service: Service, envInstance: InvInstance) {
    this.project = service.project
    this.deployHost = envInstance.host
    this.deployFolder = envInstance.folder
}
