package org.neo.gomina.plugins.ssh

import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

class DumbSshConnector : InstancesExt {

    @Inject private lateinit var sshConnector: SshConnector
    @Inject private lateinit var inventory: Inventory

    override fun onGetInstances(env: String, instances: Instances) {
        sshConnector.analyze()
        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = instances.get(id)
                    instance?.applySsh(sshConnector.getDetails(envInstance.host, envInstance.folder))
                }
            }
        }
    }

}