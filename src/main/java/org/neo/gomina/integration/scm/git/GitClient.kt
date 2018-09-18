package org.neo.gomina.integration.scm.git

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.neo.gomina.integration.scm.*
import java.io.File
import java.util.*


/**
 * https://git-scm.com/book/pl/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
 */
class GitClient : ScmClient {
    companion object {
        private val logger = LogManager.getLogger(GitClient::class.java)
    }

    val repository: Repository
    val git:Git

    constructor(url: String) {
        this.repository = FileRepositoryBuilder().setGitDir(File(url)).build()
        this.git = Git(repository)
    }

    private val masterName = "refs/heads/master"

    override fun getTrunk(url: String): String {
        return masterName
    }

    override fun getBranches(url: String): List<Branch> {
        logger.info("Retrieve Branches")
        val branches = git.branchList().call()
        val result = branches.map {
            val branchName = it.name
            val originRev = if (branchName != masterName) {
                val walk = RevWalk(repository)
                val from = repository.resolve(branchName)
                val to = repository.resolve(masterName)
                walk.markStart(walk.parseCommit(from))
                walk.markUninteresting(walk.parseCommit(to))

                walk.map { it.parents.firstOrNull()?.name }.lastOrNull()
            }
            else null

            Branch(name = branchName, originRevision = originRev)
        }
        logger.info("Retrieved ${result.size} Branches")
        return result
    }

    override fun getLog(url: String, branch: String, rev: String, count: Int): List<Commit> {
        //git.branchList().call().forEach { println("branch ${it.name}") }
        //val master = repository.getRef(folder)
        //println("$folder -> $master")
        val commits = git.log().add(repository.resolve(branch)).setMaxCount(100).call()
        return commits.map {
            Commit(
                    revision = it.name,
                    date = Date(it.commitTime.toLong() * 1000L),
                    author = it.authorIdent.name,
                    //author = it.committerIdent.name,
                    message = "${StringUtils.replaceChars(it.fullMessage, "\n", " ")}"
                    //(${it.parents.map { it.tree.type }})
            )
        }
    }

    override fun getFile(url: String, rev: String): String? {
        // FIXME Implement
        return null
    }

    override fun listFiles(url: String, rev: String): List<String> {
        // FIXME Implement
        return arrayListOf()
    }
}