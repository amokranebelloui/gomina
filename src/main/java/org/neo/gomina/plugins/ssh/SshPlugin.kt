package org.neo.gomina.plugins.ssh

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.Plugin
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class SshPlugin : Plugin {

    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var inventory: Inventory

    private val sshCache = Cache<SshDetails>("ssh")

    fun enrich(instance: InvInstance, detail: Instance) {
        if (!instance.host.isNullOrBlank() && !instance.folder.isNullOrBlank()) {
            sshCache.get("${instance.host}-${instance.folder}")?. let { detail.applySsh(it) }
        }
    }

    override fun init() {
        logger.info("Initializing instances SSH data")
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
                        val host = it.host!!
                        val folder = it.folder!!
                        sshCache.cache("$host-$folder", sshDetails)
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
    this.confRevision = sshDetails.confRevision
}

