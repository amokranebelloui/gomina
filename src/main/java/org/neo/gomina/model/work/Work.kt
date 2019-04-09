package org.neo.gomina.model.work

enum class WorkStatus {
    OFF, SPEC, DEV, TESTING, DONE, DEPLOYMENT, COMPLETED;
    fun isOpen() = this != COMPLETED
}

data class Work(val id: String, val label: String, val type: String?, val jira: String?,
                val status: WorkStatus = WorkStatus.OFF,
                val people: List<String> = emptyList(),
                val components: List<String> = emptyList(),
                val archived: Boolean = false
)

interface WorkList {
    fun getAll(): List<Work>
    fun get(workId: String): Work?
    fun addWork(label: String?, type: String?, jira: String?, people: List<String>, components: List<String>): String
    fun updateWork(workId: String, label: String?, type: String?, jira: String?, people: List<String>, components: List<String>)
    fun archiveWork(workId: String)
    fun unarchiveWork(workId: String)
}