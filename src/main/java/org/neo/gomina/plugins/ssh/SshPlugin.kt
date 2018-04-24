package org.neo.gomina.plugins.ssh

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.Plugin
import javax.inject.Inject

class SshPlugin : Plugin {

    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var inventory: Inventory

    private val sshCache = SshCache()

    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    override fun init() {
        logger.info("Initializing instances SSH data ...")
        inventory.getEnvironments().forEach { env ->
            env.services
                    .flatMap { it.instances }
                    .filter { !it.host.isNullOrBlank() }
                    .filter { !it.folder.isNullOrBlank() }
                    .forEach {
                        val id = env.id + "-" + it.id
                        sshCache.getDetail(it.host!!, it.folder!!) ?. let {
                            instanceDetailRepository.getInstance(id)?.applySsh(it)
                        }
                    }
        }
        logger.info("Instances SSH data initialized")
    }

    fun reloadInstances(envId: String) {
        inventory.getEnvironment(envId)?.let { env ->
            val analysis = sshConnector.analyze(env)
            env.services
                    .flatMap { it.instances }
                    .filter { !it.host.isNullOrBlank() }
                    .filter { !it.folder.isNullOrBlank() }
                    .forEach {
                        val sshDetails = analysis.getFor(it.host, it.folder)
                        sshCache.cacheDetail(it.host!!, it.folder!!, sshDetails)
                        val id = env.id + "-" + it.id
                        instanceDetailRepository.getInstance(id)?.applySsh(sshDetails)
                    }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(SshPlugin::class.java)
    }
}

fun Instance.applySsh(sshDetails: SshDetails) {
    this.deployVersion = sshDetails.deployedVersion
    this.deployRevision = sshDetails.deployedRevision
    this.confCommited = sshDetails.confCommitted
    this.confUpToDate = sshDetails.confUpToDate
}

