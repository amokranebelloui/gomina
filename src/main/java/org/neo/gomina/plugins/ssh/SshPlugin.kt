package org.neo.gomina.plugins.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.InstanceDetail
import org.neo.gomina.integration.ssh.SshClient
import org.neo.gomina.integration.ssh.SshDetails
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class SshPlugin {

    @Inject private lateinit var sshConnector: SshOnDemandConnector
    @Inject private lateinit var inventory: Inventory

    private val sshCache = Cache<SshDetails>("ssh")

    fun enrich(instance: Instance, detail: InstanceDetail) {
        if (!instance.host.isNullOrBlank() && !instance.folder.isNullOrBlank()) {
            sshCache.get("${instance.host}-${instance.folder}")?. let { detail.applySsh(it) }
        }
    }

    fun init() {
        logger.info("Initializing instances SSH data")
    }

    fun reloadInstances(envId: String) {
        inventory.getEnvironment(envId)?.let { env ->
            val analysis = sshConnector.analyze(env) { instance, sshClient, session, prefix, sshDetails ->
                sshDetails.analyzed = true
                sshDetails.deployedVersion = deployedVersion(sshClient, session, instance.folder, prefix)
                sshDetails.deployedRevision = null
                sshDetails.confRevision = confRevision(sshClient, session, instance.folder, prefix)
                sshDetails.confCommitted = checkConfCommited(sshClient, session, instance.folder, prefix)
                sshDetails.confUpToDate = null
            }
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

    fun actions(sshClient: SshClient, session: Session, applicationFolder: String, version: String) {
        val deploy = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/release.sh $version"
        val run = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/run-all.sh"
        val stop = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/stop-all.sh"
        val whomia = "whoami"
        //val result = executeCommand(session, cmd)
    }

    fun checkConfCommited(sshClient: SshClient, session: Session, applicationFolder: String?, prefix: String): Boolean? {
        val result = sshClient.executeCommand(session, "$prefix svn status $applicationFolder/config")
        return if (StringUtils.isBlank(result)) java.lang.Boolean.TRUE else if (result.contains("is not a working copy")) null else java.lang.Boolean.FALSE
    }

    fun confRevision(sshClient: SshClient, session: Session, applicationFolder: String?, prefix: String): String? {
        val result = sshClient.executeCommand(session, "$prefix svn info $applicationFolder/config | grep Revision: |cut -c11-")
        return when {
            result.contains("does not exist") -> "?"
            result.contains("is not a working copy") -> "!svn"
            else -> result
        }
    }

    fun deployedVersion(sshClient: SshClient, session: Session, applicationFolder: String?, prefix: String): String {
        var result = sshClient.executeCommand(session, "$prefix cat $applicationFolder/current/version.txt 2>/dev/null")
        result = StringUtils.trim(result)
        if (StringUtils.isBlank(result)) {
            result = sshClient.executeCommand(session, "$prefix ls -ll $applicationFolder/current")
            val pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/"
            result = result.replace(pattern.toRegex(), "$1").trim { it <= ' ' }
        }
        return result
    }

    companion object {
        private val logger = LogManager.getLogger(SshPlugin::class.java)
    }
}

fun InstanceDetail.applySsh(sshDetails: SshDetails) {
    this.deployVersion = sshDetails.deployedVersion
    this.deployRevision = sshDetails.deployedRevision
    this.confCommited = sshDetails.confCommitted
    this.confUpToDate = sshDetails.confUpToDate
    this.confRevision = sshDetails.confRevision
}

