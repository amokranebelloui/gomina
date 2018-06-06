package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.utils.Cache
import javax.inject.Inject

data class InstanceSshDetails(
        var analyzed: Boolean = false,
        var deployedVersion: String? = null,
        var deployedRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null
)

data class HostSshDetails(
        var analyzed: Boolean = false,
        var unexpectedFolders: List<String> = emptyList()
)

interface SshAnalysis {
    fun instance(instance: Instance, session: Session, sudo: String?): InstanceSshDetails = InstanceSshDetails(analyzed = true)
    fun host(session: Session, sudo: String?): HostSshDetails = HostSshDetails(analyzed = true)
}

class SshService {

    @Inject private lateinit var sshAnalysis: SshAnalysis
    @Inject private lateinit var sshConnector: SshOnDemandConnector
    private val sshCache = Cache<InstanceSshDetails>("ssh")
    private val sshHostCache = Cache<HostSshDetails>("ssh-host")

    fun getDetails(instance: Instance): InstanceSshDetails? {
        if (!instance.host.isNullOrBlank() && !instance.folder.isNullOrBlank()) {
            return sshCache.get("${instance.host}-${instance.folder}")
        }
        return null
    }

    fun getDetails(host: String): HostSshDetails? {
        return sshHostCache.getOrLoad(host)
    }

    fun processEnv(env: Environment) {
        val analysis = sshConnector.analyze(env) { instance, session, sudo ->
            sshAnalysis.instance(instance, session, sudo)
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