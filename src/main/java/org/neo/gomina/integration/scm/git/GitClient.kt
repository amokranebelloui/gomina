package org.neo.gomina.integration.scm.git

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import org.neo.gomina.integration.scm.impl.CommitDecorator
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmBranch
import org.neo.gomina.model.scm.ScmClient
import java.io.File
import java.time.Instant
import java.time.ZoneOffset


/**
 * https://git-scm.com/book/pl/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
 */
class GitClient : ScmClient {
    companion object {
        private val logger = LogManager.getLogger(GitClient::class.java)
    }

    internal val cp:CredentialsProvider?
    internal val local:Boolean
    internal val repository: Repository
    internal val git:Git
    private val commitDecorator = CommitDecorator()

    constructor(url: String, username: String? = null, password: String? = null, local: Boolean = false) {
        this.local = local

        if (!local) {
            this.cp = UsernamePasswordCredentialsProvider(username, password)
            val name = url.replace('\\', '_')
                    .replace('/', '_')
                    .replace(':', '_')
            val location = ".cache\\git\\$name"
            val folder = File(location)
            if (!folder.exists()) {
                Git.cloneRepository()
                        .setURI(url)
                        .setDirectory(folder)
                        .setCloneAllBranches(true)
                        .setCredentialsProvider(cp)
                        .call()
            }
            this.repository = FileRepositoryBuilder().setGitDir(File("$location\\.git")).build()
        }
        else {
            this.cp = null
            this.repository = FileRepositoryBuilder().setGitDir(File(url)).build()
        }
        this.git = Git(repository)
    }

    private val masterName: String get() = if (local) "refs/heads/master" else "refs/remotes/origin/master"

    override fun getTrunk(): String {
        return masterName
    }

    override fun getBranches(): List<ScmBranch> {
        logger.info("Retrieve Branches")
        val branches = git.branchList()
                .let { if (local) it.setListMode(ListMode.REMOTE) else it }
                .call()
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

            ScmBranch(name = branchName, originRevision = originRev)
        }
        logger.info("Retrieved ${result.size} Branches")
        return result
    }

    override fun getLog(branch: String, rev: String, count: Int): List<Commit> {
        //git.branchList().call().forEach { println("branch ${it.name}") }
        //val master = repository.getRef(folder)
        //println("$folder -> $master")
        git.pull()
                .let { if (!local) it.setCredentialsProvider(cp) else it }
                .call()
        val commits = git.log().add(repository.resolve(branch)).setMaxCount(count).call()
        return commits.map {
            val revision = it.name
            val message = StringUtils.replaceChars(it.fullMessage, "\n", " ")
            val version = commitDecorator.flag(branch, revision, message, this)
            Commit(
                    revision = revision,
                    date = Instant.ofEpochMilli(it.commitTime.toLong() * 1000L).atZone(ZoneOffset.UTC).toLocalDateTime(),
                    author = it.authorIdent.name,
                    //author = it.committerIdent.name,
                    message = message,
                    branches = listOf(branch),
                    version = version
                    //(${it.parents.map { it.tree.type }})
            )
        }
    }

    override fun getFile(branch : String, url: String, rev: String): String? {
        //val trunk = getTrunk()
        val head = repository.getRef(branch)
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