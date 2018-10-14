package org.neo.gomina.model.scm

import java.util.*

data class Commit (
    var revision: String = "",
    var date: Date? = null,
    var author: String? = null,
    var message: String? = null,

    var release: String? = null, // new version: if the commit is a prepare release version change
    var newVersion: String? = null // version: if the commit is a release
)

data class Branch(
        var name: String,
        var origin: String? = null,
        var originRevision: String? = null
)

interface ScmClient {

    fun getTrunk(): String = ""

    fun getBranches(): List<Branch> = arrayListOf()

    /** Get log from HEAD to revision, max @count elements */
    fun getLog(branch: String, rev: String, count: Int): List<Commit>

    /** get file for a revision, HEAD is -1 **/
    fun getFile(url: String, rev: String): String?

    fun listFiles(url: String, rev: String): List<String>
}

