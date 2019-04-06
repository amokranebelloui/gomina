package org.neo.gomina.model.version

import org.apache.commons.lang3.StringUtils

data class Version(val version: String = "", val revision: String?) : Comparable<Version> {

    companion object {
        fun isSnapshot(version: String) = version.endsWith("-SNAPSHOT")
        fun isStable(version: String) = !isSnapshot(version)
        fun from(version: String?, revision: String?) =
                version?.let { Version(it, revision) }
        fun of(version: String?, revision: String?) =
                if (version?.isNotEmpty() == true) Version(version, revision) else null
    }

    constructor(version: String = "") : this(version, null)

    fun isSnapshot() = isSnapshot(version)
    fun isStable() = isStable(version)

    /*
    override fun equals(other: Any?): Boolean {
        return other is Version && versionCompare(this.version, other.version) == 0
    }
    */

    override fun compareTo(other: Version): Int {
        val res = versionCompare(this.version, other.version)
        return res
        /*
        return if (res == 0 && this.isSnapshot()) {
            //Integer.signum((this.revision - other.revision).toInt())
            if (this.revision > other.revision) 1 else if (this.revision < other.revision) -1 else 0
        } else res
        */
    }
    fun clean() = if (this.isSnapshot()) this else Version(this.version)
    override fun toString() = version + "@" + revision
}

// FIXME Function to order snapshots

private fun versionCompare(str1: String?, str2: String?): Int {
    var str1 = str1
    var str2 = str2
    if (StringUtils.isBlank(str1) && StringUtils.isBlank(str2)) {
        return 0
    }
    if (StringUtils.isBlank(str2)) {
        return 1
    }
    if (StringUtils.isBlank(str1)) {
        return -1
    }
    val str1snapshot = if (str1 != null && str1.contains("-SNAPSHOT")) 0 else 1
    val str2snapshot = if (str2 != null && str2.contains("-SNAPSHOT")) 0 else 1

    str1 = if (str1 != null) str1.replace("-SNAPSHOT", "") else "0"
    str2 = if (str2 != null) str2.replace("-SNAPSHOT", "") else "0"

    val vals1 = str1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val vals2 = str2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    var i = 0
    // set index to first non-equal ordinal or length of shortest version string
    while (i < vals1.size && i < vals2.size && vals1[i] == vals2[i]) {
        i++
    }
    // compare first non-equal ordinal number
    if (i < vals1.size && i < vals2.size) {
        val diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]))
        return Integer.signum(diff)
    } else return if (vals1.size == vals2.size) {
        Integer.signum(str1snapshot - str2snapshot)
    } else {
        Integer.signum(vals1.size - vals2.size)
    }// the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
}