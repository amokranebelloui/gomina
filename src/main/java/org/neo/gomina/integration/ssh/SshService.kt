package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

interface SshAnalysis {
    fun instances(session: Session, sudo: String?, instances: List<Instance>): Map<String, InstanceSshDetails> = emptyMap()
    fun host(session: Session, sudo: String?): HostSshDetails = HostSshDetails(analyzed = true)
}

class SshService {

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var hosts: Hosts
    @Inject private lateinit var sshAnalysis: SshAnalysis
    @Inject private lateinit var sshConnector: SshOnDemandConnector

    fun processEnv(env: Environment) {
        val analysis = sshConnector.analyze(env) { session, sudo, instances ->
            sshAnalysis.instances(session, sudo, instances)
        }
        env.services.forEach { service ->
            service.instances
                .filter { !it.host.isNullOrBlank() }
                .filter { !it.folder.isNullOrBlank() }
                .forEach { instance ->
                    analysis.getFor(instance.host, instance.folder)?.let {
                        inventory.updateDeployedRevision(env.id, service.svc, instance.id, it.version)
                        inventory.updateConfigStatus(env.id, service.svc, instance.id,
                                it.confRevision, it.confCommitted, it.confUpToDate)
                    }
                }
        }

    }

    fun processHost(host: String) {
        val result: HostSshDetails? = sshConnector.analyze(host) { session, sudo ->
            sshAnalysis.host(session, sudo)
        }
        result?.let {
            hosts.updateUnexpectedFolders(host, result.unexpectedFolders)
        }
    }

}