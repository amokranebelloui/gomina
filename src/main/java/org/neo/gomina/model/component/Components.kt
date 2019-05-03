package org.neo.gomina.model.component

import org.neo.gomina.model.scm.Branch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.system.System
import org.neo.gomina.model.version.Version
import java.time.LocalDate
import java.time.LocalDateTime

data class Component(
        var id: String,
        var label: String? = null,
        var type: String? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scm: Scm? = null,
        var hasMetadata: Boolean,
        var artifactId: String? = null,
        var sonarServer: String = "",
        var jenkinsServer: String = "",
        var jenkinsJob: String? = null,

        var inceptionDate: LocalDate? = null,
        var owner: String? = null,
        var criticity: Int? = null,
        var latest: Version? = null,
        var released: Version? = null,
        var changes: Int? = null,
        var branches: List<Branch> = emptyList(),
        var docFiles: List<String> = emptyList(),
        var lastCommit: LocalDateTime?,
        var commitActivity: Int,
        var commitToRelease: Int?,

        var loc: Double? = null,
        var coverage: Double? = null,

        var buildNumber: String? = null,
        var buildStatus: String? = null,
        var buildBuilding: Boolean? = null,
        var buildTimestamp: Long? = null,

        var disabled: Boolean
) {
    fun shareSystem(other: Component): Boolean {
        return System.extend(this.systems).intersect(System.extend(other.systems)).isNotEmpty()
    }
}

data class NewComponent(
        var id: String,
        var label: String? = null,
        var artifactId: String? = null,
        var type: String? = null,
        //var owner: String? = null,
        //var criticity: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scm: Scm? = null,
        var hasMetadata: Boolean,
        var sonarServer: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null
)


data class Scm (
        var type: String = "",
        var url: String = "", var path: String = "",
        val username: String = "", val passwordAlias: String = "") {

    val id: String get() = "$type-$url-$path"
    val fullUrl: String get() = "$url" + if (path.isNotBlank()) "/$path" else "$path"
}


interface ComponentRepo {
    fun getAll(): List<Component>
    fun get(componentId: String): Component?
    fun getCommitLog(componentId: String): List<Commit>
    fun add(component: NewComponent)
    fun editLabel(componentId: String, label: String)
    fun editType(componentId: String, type: String)
    fun editInceptionDate(componentId: String, inceptionDate: LocalDate?)
    fun editOwner(componentId: String, owner: String?) // TODO Overridable
    fun editCriticity(componentId: String, criticity: Int?) // TODO Overridable
    fun editArtifactId(componentId: String, artifactId: String?) // TODO Overridable
    fun editScm(componentId: String, type: String, url: String, path: String?, hasMetadata: Boolean)
    fun editSonar(componentId: String, server: String?)
    fun editBuild(componentId: String, server: String?, job: String?)
    fun addSystem(componentId: String, system: String)
    fun deleteSystem(componentId: String, system: String)
    fun addLanguage(componentId: String, language: String)
    fun deleteLanguage(componentId: String, language: String)
    fun addTag(componentId: String, tag: String)
    fun deleteTag(componentId: String, tag: String)
    fun disable(componentId: String)
    fun enable(componentId: String)
    fun updateCodeMetrics(componentId: String, loc: Double?, coverage: Double?)
    fun updateBuildStatus(componentId: String, number: String?, status: String?, building: Boolean?, timestamp: Long?)
    fun updateVersions(componentId: String, latest: Version?, released: Version?, changes: Int?)
    fun updateBranches(componentId: String, branches: List<Branch>)
    fun updateDocFiles(componentId: String, docFiles: List<String>)
    fun updateCommitLog(componentId: String, commits: List<Commit>)
    fun updateLastCommit(componentId: String, lastCommit: LocalDateTime?)
    fun updateCommitActivity(componentId: String, activity: Int)
    fun updateCommitToRelease(componentId: String, commitToRelease: Int?)
}



