package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.neo.gomina.model.host.HostRepo
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.utils.Cache
import javax.inject.Inject

interface SshAnalysis {
    fun instances(session: Session, sudo: String?, instances: List<Instance>): Map<String, InstanceSshDetails> = emptyMap()
    fun host(session: Session, sudo: String?): HostSshDetails = HostSshDetails(analyzed = true)
}

class SshService : HostRepo {

    @Inject private lateinit var sshAnalysis: SshAnalysis
    @Inject private lateinit var sshConnector: SshOnDemandConnector
    private val sshCache = Cache<InstanceSshDetails>("ssh")
    private val sshHostCache = Cache<HostSshDetails>("ssh-host")

    override fun getDetails(instance: Instance): InstanceSshDetails? {
        if (!instance.host.isNullOrBlank() && !instance.folder.isNullOrBlank()) {
            return sshCache.get("${instance.host}-${instance.folder}")
        }
        return null
    }

    fun getDetails(host: String): HostSshDetails? {
        return sshHostCache.getOrLoad(host)
    }

    fun processEnv(env: Environment) {
        val analysis = sshConnector.analyze(env) { session, sudo, instances ->
            sshAnalysis.instances(session, sudo, instances)
        }
        env.services
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() }
                .filter { !it.folder.isNullOrBlank() }
                .forEach {
                    val sshDetails = analysis.getFor(it.host, it.folder) ?: InstanceSshDetails()
                    val host = it.host!!
                    val folder = it.folder!!
                    sshCache.cache("$host-$folder", sshDetails)
                }
    }

    fun processHost(host: String) {
        val result = sshConnector.analyze(host) { session, sudo ->
            sshAnalysis.host(session, sudo)
        }
        result?.let { sshHostCache.cache("$host", result) }
    }

}