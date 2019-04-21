package org.neo.gomina.integration.scm.git

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.neo.gomina.model.scm.Branch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
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

    override fun getTrunk(): String {
        return masterName
    }

    override fun getBranches(): List<Branch> {
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

    override fun getLog(branch: String, rev: String, count: Int): List<Commit> {
        //git.branchList().call().forEach { println("branch ${it.name}") }
        //val master = repository.getRef(folder)
        //println("$folder -> $master")
        val commits = git.log().add(repository.resolve(branch)).setMaxCount(100).call()
        return commits.map {
            Commit(
                    revision = it.name,
                    date = Instant.ofEpochMilli(it.commitTime.toLong() * 1000L).atZone(ZoneOffset.UTC).toLocalDateTime(),
                    author = it.authorIdent.name,
                    //author = it.committerIdent.name,
                    message = "${StringUtils.replaceChars(it.fullMessage, "\n", " ")}"
                    //(${it.parents.map { it.tree.type }})
            )
        }
    }

    override fun getFile(url: String, rev: String): String? {
        val trunk = getTrunk()
        val head = repository.getRef(trunk)
        val walk = RevWalk(repository)
        val commit = walk.parseCommit(head.getObjectId())
        val tree = commit.tree

        val reader = repository.newObjectReader()
        try {
            val treeWalk = TreeWalk.forPath(repository, url, tree)
            return if (treeWalk != null) {
                // use the blob id to read the file's data
                val data = reader.open(treeWalk.getObjectId(0)).bytes
                String(data, Charsets.UTF_8)
            }
            else null
        }
        finally {
            reader.release()
        }
    }

    override fun listFiles(url: String, rev: String): List<String> {
        val head = repository.getRef(masterName)
        val walk = RevWalk(repository)
        val commit = walk.parseCommit(head.getObjectId())
        val tree = commit.tree

        val treeWalk =
                if (url.isNotBlank() && url != "/") TreeWalk.forPath(repository, url, tree)
                else TreeWalk(repository).apply { addTree(tree) }

        treeWalk.isRecursive = true
        val files = mutableListOf<String>()
        while (treeWalk.next()) {
            if (treeWalk.isSubtree) {
                //println("dir: " + treeWalk.pathString)
                treeWalk.enterSubtree()
            }
            else {
                //println("file: " + treeWalk.pathString)
                files.add(treeWalk.pathString)
            }
        }
        return files
    }
}