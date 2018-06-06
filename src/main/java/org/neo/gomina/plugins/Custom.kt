package org.neo.gomina.plugins

import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.ssh.HostSshDetails
import org.neo.gomina.integration.ssh.SshAnalysis
import org.neo.gomina.integration.ssh.InstanceSshDetails
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.model.inventory.Instance

class CustomSshAnalysis : SshAnalysis {

    override fun instance(instance: Instance, session: Session, sudo: String?): InstanceSshDetails {
        return InstanceSshDetails(
                analyzed = true,
                deployedVersion = deployedVersion(session, sudo, instance.folder),
                deployedRevision = null,
                confRevision = confRevision(session, sudo, instance.folder),
                confCommitted = checkConfCommited(session, sudo, instance.folder),
                confUpToDate = null
        )
    }

    override fun host(session: Session, sudo: String?): HostSshDetails {
        val result = session.sudo(sudo, "find /Users/Test/Work -mindepth 1 -maxdepth 1 -type d")
        return when {
            result.contains("No such file or directory") -> HostSshDetails(analyzed = true)
            else -> {
                val unexpected = result.split("\n").filter { it.isNotBlank() }.map { it.trim() }
                HostSshDetails(analyzed = true, unexpectedFolders = unexpected)
            }
        }
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

}