package org.neo.gomina.integration.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

interface SshAnalysis {
    fun instancesSSH(host: Host, session: Session, instances: List<Instance>): Map<String, InstanceSshDetails> = emptyMap()
    fun instancesDummy(host: Host, instances: List<Instance>): Map<String, InstanceSshDetails> = emptyMap()
    fun hostSSH(host: Host, session: Session): HostSshDetails = HostSshDetails(analyzed = true)
    fun hostDummy(host: Host): HostSshDetails = HostSshDetails(analyzed = true)
}

class SshService {

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var hosts: Hosts
    @Inject private lateinit var sshAnalysis: SshAnalysis
    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var dummyHostConnector: DummyHostConnector

    fun processEnv(env: Environment) {
        logger.info("SSH Analysis for ${env.id}")
        val result = mutableMapOf<String, Map<String, InstanceSshDetails>?>()
        env.services
                .flatMap { it.instances }
                .filter { !it.host.isNullOrBlank() && !it.folder.isNullOrBlank() }
                .groupBy { it.host!! }
                .mapNotNull { (hostname, instances) -> hosts.getHost(hostname)?.let { it to instances } }
                .forEach { (host, instances) ->
                    logger.info("${instances.size} instances to analyze")
                    result[host.host] = when (host.osFamily) {
                        "linux", "unix", "macos" ->
                            sshConnector.process(host) { s -> sshAnalysis.instancesSSH(host, s, instances) }
                        "dummy" ->
                            dummyHostConnector.process(host) { sshAnalysis.instancesDummy(host, instances) }
                        else ->
                            null
                    }
                    logger.info("Analyzed ${instances.size} apps on ${host.host}")
                }
        logger.info("SSH Analysis for ${env.id} done")

        env.services.forEach { service ->
            service.instances
                .filter { !it.host.isNullOrBlank() }
                .filter { !it.folder.isNullOrBlank() }
                .forEach { instance ->
                    result.getFor(instance.host, instance.folder)?.let {
                        inventory.updateDeployedRevision(env.id, service.svc, instance.id, it.version)
                        inventory.updateConfigStatus(env.id, service.svc, instance.id,
                                it.confRevision, it.confCommitted, it.confUpToDate)
                    }
                }
        }
    }

    fun processHost(hostname: String) {
        logger.info("SSH Analysis for $hostname")

        hosts.getHost(hostname)?.let { host ->
            var result = when (host.osFamily) {
                "linux", "unix", "macos" ->
                    sshConnector.process(host) { s -> sshAnalysis.hostSSH(host, s) }
                "dummy" ->
                    dummyHostConnector.process(host) { sshAnalysis.hostDummy(host) }
                else ->
                    null
            }
            result?.let {
                hosts.updateUnexpectedFolders(hostname, it.unexpectedFolders)
            }
        }

    }

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

}

fun <T> Map<String, Map<String, T>?>.getFor(host: String?, folder: String?): T? {
    var result: T? = null
    if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(folder)) {
        val servers = this[host]
        if (servers != null) {
            result = servers[folder]
        }
    }
    return result
}
