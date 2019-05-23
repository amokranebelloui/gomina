package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.diagram.Diagram
import org.neo.gomina.model.diagram.DiagramComponent
import org.neo.gomina.model.diagram.DiagramRef
import org.neo.gomina.model.diagram.Diagrams
import redis.clients.jedis.JedisPool

class RedisDiagrams : Diagrams {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 9)
        logger.info("Diagrams Database connected $host $port")
    }

    override fun all(): List<DiagramRef> {
        pool.resource.use { jedis ->
            return jedis.keys("diagram:*").map {
                val diagramId = it.replace(Regex("^diagram:"), "")
                jedis.hgetAll(it).toDiagramRef(diagramId)
            }
        }
    }

    private fun Map<String, String>.toDiagramRef(diagramId: String): DiagramRef {
        return DiagramRef(
                diagramId = diagramId,
                name = this["name"] ?: diagramId,
                description = this["description"]
        )
    }

    override fun get(diagramId: String): Diagram {
        pool.resource.use { jedis ->
            val ref = jedis.hgetAll("diagram:$diagramId").toDiagramRef(diagramId)
            val components = jedis.zrangeWithScores("diagram_detail:$diagramId", 0, -1).map {
                val coordinates = it.score.toCoordinates
                DiagramComponent(it.element, coordinates.first, coordinates.second)
            }
            return Diagram(ref, components)
        }
    }

    override fun update(diagramId: String, component: DiagramComponent) {
        pool.resource.use { jedis ->
            jedis.zadd("diagram_detail:$diagramId", component.coordinatesAsDouble, component.name)
        }
    }

    override fun remove(diagramId: String, name: String) {
        pool.resource.use { jedis ->
            jedis.zrem("diagram_detail:$diagramId", name)
        }
    }

    override fun add(diagramId: String, name: String, description: String) {
        pool.resource.use { jedis ->
            val key = "diagram:$diagramId"
            if (jedis.exists(key)) {
                logger.error("Diagram $diagramId already exist")
            }
            else {
                jedis.persist(key, mapOf(
                        "name" to name,
                        "description" to description
                ))
            }
        }
    }

    override fun delete(diagramId: String): Boolean {
        pool.resource.use { jedis ->
            if (!jedis.exists("diagram_detail:$diagramId")) {
                jedis.del("diagram:$diagramId", diagramId)
                return true
            }
        }
        return false
    }
}

val FOUR_POSITIONS = 10000

val DiagramComponent.coordinatesAsDouble get() = (this.x * FOUR_POSITIONS + this.y).toDouble()
val Double.toCoordinates get() = (this / FOUR_POSITIONS).toInt() to (this % FOUR_POSITIONS).toInt()

fun main(args: Array<String>) {
    println(DiagramComponent("some", 650, 230).coordinatesAsDouble)
    println((1230440.0).toCoordinates)
}
