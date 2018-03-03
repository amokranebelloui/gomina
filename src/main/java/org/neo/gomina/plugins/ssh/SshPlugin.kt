package org.neo.gomina.plugins.ssh

import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.ssh.impl.SshDetails
import org.neo.gomina.plugins.ssh.impl.SshOnDemandConnector
import javax.inject.Inject

class SshPlugin : InstancesExt {

    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var inventory: Inventory

    override fun onGetInstances(envId: String, instances: Instances) {
        inventory.getEnvironment(envId)?.let { env ->
            val analysis = sshConnector.analyze(env)
            env.services
                .flatMap { it.instances }
                .forEach {
                    val id = env.id + "-" + it.id
                    instances.get(id)?.applySsh(analysis.getFor(it.host, it.folder))
                }
        }
    }
}

fun Instance.applySsh(sshDetails: SshDetails) {
    this.deployVersion = sshDetails.deployedVersion
    this.deployRevision = sshDetails.deployedRevision
    this.confCommited = sshDetails.confCommitted
    this.confUpToDate = sshDetails.confUpToDate
}

