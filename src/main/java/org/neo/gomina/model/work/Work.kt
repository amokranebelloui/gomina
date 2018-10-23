package org.neo.gomina.model.work

enum class WorkStatus {
    OFF, SPEC, DEV, TESTING, DONE, DEPLOYMENT, COMPLETED;
    fun isOpen() = this != COMPLETED
}

data class Work(val id: String, val label: String, val type: String?, val jira: String?,
                val status: WorkStatus = WorkStatus.OFF,
                val people: List<String> = emptyList(),
                val components: List<String> = emptyList()
)

interface WorkList {
    fun getAll(): List<Work>
}