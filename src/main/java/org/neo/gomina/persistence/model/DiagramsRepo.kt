package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.diagram.Diagram
import org.neo.gomina.model.diagram.DiagramComponent
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

    override fun getAll(): List<String> {
        pool.resource.use { jedis ->
            return jedis.keys("diagram_detail:*").map {
                it.replace(Regex("^diagram_detail:"), "")
            }
        }
    }

    override fun get(diagramId: String): Diagram {
        pool.resource.use { jedis ->
            val components = jedis.zrangeWithScores("diagram_detail:$diagramId", 0, -1).map {
                val coordinates = it.score.toCoordinates
                DiagramComponent(it.element, coordinates.first, coordinates.second)
            }
            return Diagram(components)
        }
    }

    override fun update(diagramId: String, component: DiagramComponent) {
        pool.resource.use { jedis ->
            jedis.zadd("diagram_detail:$diagramId", component.coordinatesAsDouble, component.name)
        }
    }
}

val FOUR_POSITIONS = 10000

val DiagramComponent.coordinatesAsDouble get() = (this.x * FOUR_POSITIONS + this.y).toDouble()
val Double.toCoordinates get() = (this / FOUR_POSITIONS).toInt() to (this % FOUR_POSITIONS).toInt()

fun main(args: Array<String>) {
    println(DiagramComponent("some", 650, 230).coordinatesAsDouble)
    println((1230440.0).toCoordinates)
}
