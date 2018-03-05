package org.neo.gomina.plugins.ssh

import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

class SshPlugin : InstancesExt {

    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var inventory: Inventory

    private val sshCache = SshCache()

    override fun onGetInstances(envId: String, instances: Instances) {
        inventory.getEnvironment(envId)?.let { env ->
            env.services
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() }
                .filter { !it.folder.isNullOrBlank() }
                .forEach {
                    val id = env.id + "-" + it.id
                    sshCache.getDetail(it.host!!, it.folder!!) ?. let {
                        instances.get(id)?.applySsh(it)
                    }
                }
        }
    }

    override fun onReloadInstances(envId: String) {
        inventory.getEnvironment(envId)?.let { env ->
            val analysis = sshConnector.analyze(env)
            env.services
                    .flatMap { it.instances }
                    .filter { !it.host.isNullOrBlank() }
                    .filter { !it.folder.isNullOrBlank() }
                    .forEach {
                        val sshDetails = analysis.getFor(it.host, it.folder)
                        sshCache.cacheDetail(it.host!!, it.folder!!, sshDetails)
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

