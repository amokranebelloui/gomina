package org.neo.gomina.model.work

import java.time.LocalDate
import java.time.LocalDateTime

enum class WorkStatus {
    OFF, SPEC, DEV, TESTING, DONE, DEPLOYMENT, COMPLETED;
    fun isOpen() = this != COMPLETED
}

data class Work(val id: String, val label: String, val type: String?, val issues: List<String>,
                val status: WorkStatus = WorkStatus.OFF,
                val people: List<String> = emptyList(),
                val components: List<String> = emptyList(),
                val creationDate: LocalDateTime?,
                val dueDate: LocalDate?,
                val archived: Boolean = false
)

interface WorkList {
    fun getAll(): List<Work>
    fun get(workId: String): Work?
    fun addWork(label: String?, type: String?, issues: List<String>,
                people: List<String>, components: List<String>,
                dueDate: LocalDate?): String
    fun updateWork(workId: String, label: String?, type: String?, issues: List<String>,
                   people: List<String>, components: List<String>,
                   dueDate: LocalDate?)
    fun archiveWork(workId: String)
    fun unarchiveWork(workId: String)
}