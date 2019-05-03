package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.ComponentKnowledge
import org.neo.gomina.model.component.Knowledge
import redis.clients.jedis.JedisPool

class RedisComponentKnowledge : ComponentKnowledge {

    companion object {
        private val logger = LogManager.getLogger(RedisComponentRepo.javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 7)
        logger.info("Component Knowledge Database connected $host $port")
    }

    override fun componentKnowledge(componentId: String): List<Pair<String, Knowledge>> {
        pool.resource.use { jedis ->
            return jedis.zrangeWithScores("knowledge:component:$componentId", 0, -1).map {
                it.element to Knowledge(it.score.toInt())
            }
        }
    }

    override fun userKnowledge(userId: String): List<Pair<String, Knowledge>> {
        pool.resource.use { jedis ->
            return jedis.zrangeWithScores("knowledge:user:$userId", 0, -1).map {
                it.element to Knowledge(it.score.toInt())
            }
        }
    }

    override fun updateKnowledge(componentId: String, userId: String, knowledge: Knowledge?) {
        pool.resource.use { jedis ->
            val score = knowledge?.knowledge?.toDouble()
            if (score != null) {
                jedis.zadd("knowledge:component:$componentId", score, userId)
                jedis.zadd("knowledge:user:$userId", score, componentId)
            }
            else {
                jedis.zrem("knowledge:component:$componentId", userId)
                jedis.zrem("knowledge:user:$userId", componentId)
            }
        }
    }

}
