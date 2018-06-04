package org.neo.gomina.plugins.ssh

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.instances.InstanceDetail
import org.neo.gomina.integration.ssh.SshOnDemandConnector
import org.neo.gomina.integration.ssh.getFor
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.utils.Cache
import javax.inject.Inject

data class SshDetails (
        var analyzed: Boolean = false,
        var deployedVersion: String? = null,
        var deployedRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null
)

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
            val analysis = sshConnector.analyze(env) { instance, session, sudo ->
                SshDetails(
                    analyzed = true,
                    deployedVersion = deployedVersion(session, sudo, instance.folder),
                    deployedRevision = null,
                    confRevision = confRevision(session, sudo, instance.folder),
                    confCommitted = checkConfCommited(session, sudo, instance.folder),
                    confUpToDate = null
                )
            }
            env.services
                    .flatMap { it.instances }
                    .filter { !it.host.isNullOrBlank() }
                    .filter { !it.folder.isNullOrBlank() }
                    .forEach {
                        val sshDetails = analysis.getFor(it.host, it.folder) ?: SshDetails()
                        val host = it.host!!
                        val folder = it.folder!!
                        sshCache.cache("$host-$folder", sshDetails)
                    }
        }
    }

    fun unexpectedFolders(host: String): List<String> {
        val result = sshConnector.analyze(host) { session, sudo ->
            val result = session.sudo(sudo, "find /Users/Test/Work -mindepth 1 -maxdepth 1 -type d")
            when {
                result.contains("No such file or directory") -> emptyList()
                else -> result.split("\n").filter { it.isNotBlank() }.map { it.trim() }
            }
        }
        return result ?: listOf("Could not analyze host $host")
        // FIXME Cache
    }

    fun actions(session: Session, user: String?, applicationFolder: String, version: String) {
        val deploy = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/release.sh $version"
        val run = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/run-all.sh"
        val stop = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/stop-all.sh"
        val whomia = "whoami"
        //val result = executeCommand(session, cmd)
    }

    fun checkConfCommited(session: Session, user: String?, applicationFolder: String?): Boolean? {
        val result = session.sudo(user, "svn status $applicationFolder/config")
        return if (StringUtils.isBlank(result)) java.lang.Boolean.TRUE else if (result.contains("is not a working copy")) null else java.lang.Boolean.FALSE
    }

    fun confRevision(session: Session, user: String?, applicationFolder: String?): String? {
        val result = session.sudo(user, "svn info $applicationFolder/config | grep Revision: |cut -c11-")
        return when {
            result.contains("does not exist") -> "?"
            result.contains("is not a working copy") -> "!svn"
            else -> result
        }
    }

    fun deployedVersion(session: Session, user: String?, applicationFolder: String?): String {
        var result = session.sudo(user, "cat $applicationFolder/current/version.txt 2>/dev/null")
        result = StringUtils.trim(result)
        if (StringUtils.isBlank(result)) {
            result = session.sudo(user, "ls -ll $applicationFolder/current")
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

