package org.neo.gomina.integration.scm

import java.util.*

data class Commit (
    var revision: String = "",
    var date: Date? = null,
    var author: String? = null,
    var message: String? = null,

    var release: String? = null, // new version: if the commit is a prepare release version change
    var newVersion: String? = null // version: if the commit is a release
)

interface Scope { val scope: String }
object Trunk : Scope { override val scope:String = "TRUNK" }
data class Branch (val name: String) : Scope {
    override val scope:String = "BRANCH"
}

interface ScmClient {

    /** Get log from HEAD to revision, max @count elements */
    fun getLog(url: String, scope: Scope, rev: String, count: Int): List<Commit>

    /** get file for a revision, HEAD is -1 **/
    fun getFile(url: String, rev: String): String?

    fun listFiles(url: String, rev: String): List<String>
}

