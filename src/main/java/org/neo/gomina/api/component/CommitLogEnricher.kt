package org.neo.gomina.api.component

import org.neo.gomina.api.common.UserRef
import org.neo.gomina.api.common.toRef
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.user.Users
import javax.inject.Inject

class CommitLogEnricher {

    @Inject private lateinit var users: Users

    fun enrichLog(log: List<Commit>, instances: List<ExtInstance>): CommitLogDetail {
        val tmp = log.map { Triple(it, mutableListOf<ExtInstance>(), mutableListOf<ExtInstance>()) }
        val unresolved = mutableListOf<ExtInstance>()
        instances.forEach { instance ->
            val runningVersion = instance.indicators?.version
            val deployedVersion = instance.sshDetails?.version
            val commitR = tmp.find { item -> runningVersion?.let { item.first.match(it) } == true }
            val commitD = tmp.find { item -> deployedVersion?.let { item.first.match(it) } == true }

            if (commitR == null && commitD == null) {
                unresolved.add(instance)
            }
            else {
                if (commitR != null) commitR.second.add(instance)
                else commitD?.third?.add(instance)
            }
        }
        return CommitLogDetail(
                log = tmp.map { (commit, running, deployed) ->
                    CommitDetail(
                            revision = commit.revision,
                            date = commit.date,
                            author = commit.author?.let { users.findForAccount(it) }?.toRef() ?: commit.author?.let { UserRef(shortName = commit.author) },
                            message = commit.message,
                            version = commit.release ?: commit.newVersion,
                            instances = running.map { it.toRef() },
                            deployments = deployed.map { it.toRef() }
                    )
                },
                unresolved = unresolved.map { it.toRef() }
        )

        /*
        val result = log.map {
            CommitLogEntry(
                    revision = it.revision,
                    date = it.date,
                    author = it.author,
                    message = it.message,
                    version = it.release ?: it.newVersion
            )
        }//.toMutableList()

        fun match(commit: CommitLogEntry, version: Version): Boolean {
            return commit.revision == version.revision ||
                    commit.version?.let { Version.isStable(it) && commit.version == version.version } == true
        }

        instances.forEach { instance ->
            val runningVersion = instance.indicators?.version
            val deployedVersion = instance.sshDetails?.version
            val commitR = result.find { commit -> runningVersion?.let { match(commit, it) } == true }
            val commitD = result.find { commit -> deployedVersion?.let { match(commit, it) } == true }

            if (commitR == null && commitD == null) {
                logger.info("@@@@ Cannot link ${instance.completeId}")
                val indexOfFirstR = result.indexOfFirst { it.revision != null && runningVersion != null && it.revision < runningVersion?.revision ?: "" }
                if (indexOfFirstR > 0) {
                    result.add(indexOfFirstR, CommitLogEntry(revision = runningVersion?.revision, instances = mutableListOf(instance.toRef())))
                }
                val indexOfFirstD = result.indexOfFirst { it.revision != null && deployedVersion != null && it.revision < deployedVersion?.revision ?: "" }
                if (indexOfFirstD > 0) {
                    result.add(indexOfFirstD, CommitLogEntry(revision = deployedVersion?.revision, deployments = mutableListOf(instance.toRef())))
                }
            }
            else {
                commitR?.let { it.instances.add(instance.toRef()) }
                commitD?.let { it.deployments.add(instance.toRef()) }
            }
        }
        return result
*/
    }

}