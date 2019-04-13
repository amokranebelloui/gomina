package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import org.neo.gomina.model.work.WorkStatus
import redis.clients.jedis.JedisPool
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RedisWorkList : WorkList {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 5)
        logger.info("Hosts Database connected $host $port")
    }

    override fun getAll(): List<Work> {
        pool.resource.use { jedis ->
            return jedis.keys("work:*").mapNotNull {
                jedis.hgetAll(it).toWork(it.substring(it.lastIndexOf(':') + 1))
            }
        }
    }

    override fun get(workId: String): Work? {
        pool.resource.use { jedis ->
            return jedis.hgetAll("work:$workId").toWork(workId)
        }
    }

    private fun Map<String, String>.toWork(id: String): Work {
        return Work(
                id = id,
                label = this["label"] ?: defaultLabel(id),
                type = this["type"],
                jira = this["jira"],
                status = this["status"] ?.let { WorkStatus.valueOf(it) } ?: WorkStatus.OFF,
                people = this["people"].toList(),
                components = this["components"].toList(),
                creationDate = this["creation_date"]?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) },
                dueDate = this["due_date"]?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) },
                archived = this["archived"]?.toBoolean() == true
        )
    }

    private fun defaultLabel(id: String) = "Work {$id}"

    override fun addWork(label: String?, type: String?, jira: String?,
                         people: List<String>, components: List<String>,
                         dueDate: LocalDate?): String {
        pool.resource.use { jedis ->
            val id = jedis.incr("_work:seq").toString()
            jedis.hmset("work:$id", listOfNotNull(
                    "label" to (label ?: defaultLabel(id)),
                    "type" to type,
                    jira?.let { "jira" to it },
                    people.let { "people" to it.toStr() },
                    components.let { "components" to it.toStr() },
                    "creation_date" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    dueDate?.let { "due_date" to dueDate.format(DateTimeFormatter.ISO_DATE) }
            ).toMap())
            return id
        }
    }

    override fun updateWork(workId: String, label: String?, type: String?, jira: String?,
                            people: List<String>, components: List<String>,
                            dueDate: LocalDate?) {
        pool.resource.use { jedis ->
            jedis.hmset("work:$workId", listOfNotNull(
                    "label" to (label ?: defaultLabel(workId)),
                    "type" to type,
                    jira?.let { "jira" to it },
                    people.let { "people" to it.toStr() },
                    components.let { "components" to it.toStr() },
                    dueDate?.let { "due_date" to dueDate.format(DateTimeFormatter.ISO_DATE) }
            ).toMap())
        }
    }

    override fun archiveWork(workId: String) {
        pool.resource.use { jedis ->
            jedis.hmset("work:$workId", mapOf("archived" to true.toString()))
        }
    }

    override fun unarchiveWork(workId: String) {
        pool.resource.use { jedis ->
            jedis.hmset("work:$workId", mapOf("archived" to false.toString()))
        }
    }
}