package org.neo.gomina.integration.scm.git

import org.apache.commons.lang3.StringUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.neo.gomina.integration.scm.*
import java.io.File
import java.util.*


/**
 * https://git-scm.com/book/pl/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
 */
class GitClient : ScmClient {
    val repository: Repository
    val git:Git

    constructor(url: String) {
        this.repository = FileRepositoryBuilder().setGitDir(File(url)).build()
        this.git = Git(repository)
    }

    override fun getLog(url: String, scope: Scope, rev: String, count: Int): List<Commit> {
        //git.branchList().call().forEach { println("branch ${it.name}") }
        //val master = repository.getRef(folder)
        //println("$folder -> $master")
        val ref = when (scope) {
            Trunk -> "refs/heads/master"
            is Branch -> "refs/heads/${scope.name}"
            else -> null
        }
        val commits = git.log().add(repository.resolve(ref)).setMaxCount(100).call()
        return commits.map {
            Commit(
                    revision = it.name,
                    date = Date(it.commitTime.toLong() * 1000L),
                    author = it.authorIdent.name,
                    //author = it.committerIdent.name,
                    message = StringUtils.replaceChars(it.fullMessage, "\n", " ")
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