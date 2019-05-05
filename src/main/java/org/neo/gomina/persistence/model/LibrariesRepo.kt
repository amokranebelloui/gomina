package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.dependency.ComponentVersion
import org.neo.gomina.model.dependency.Libraries
import org.neo.gomina.model.version.Version
import redis.clients.jedis.JedisPool

class RedisLibraries : Libraries {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 2)
        logger.info("Libraries Database connected $host $port")
    }

    override fun libraries(): Map<ArtifactId, List<Version>> {
        pool.resource.use { jedis ->
            return jedis.keys("library:*")
                    .map { it.replace(Regex("^library:"), "") }
                    .map { it.splitVersion() }
                    .map { (artifactId, version) -> ArtifactId.tryWithVersion(artifactId) to Version(version) }
                    .mapNotNull { (artifactId, version) -> artifactId?.let { artifactId to version } }
                    .groupBy( { it.first } ) { it.second }
        }
    }

    override fun library(artifactId: ArtifactId): Map<Version, List<ComponentVersion>> {
        pool.resource.use { jedis ->
            return jedis.keys("library:${artifactId.toStr()}:*")
                    .mapNotNull { key ->
                        val version = ArtifactId.tryWithVersion(key.replace(Regex("^library:"), ""))?.getVersion()
                        val componentsVersions = jedis.smembers(key).mapNotNull { it.toComponentVersion() }
                        version?.let { version to componentsVersions }
                    }
                    .toMap()
        }
    }

    override fun usedByComponent(componentId: String): List<ArtifactId> {
        pool.resource.use { jedis ->
            return jedis.keys("libraries:$componentId:*")
                    .flatMap { jedis.smembers(it) }
                    .mapNotNull { ArtifactId.tryWithVersion(it) }
        }
    }

    override fun componentsUsing(artifactId: ArtifactId): List<ComponentVersion> {
        pool.resource.use { jedis ->
            return jedis.keys("library:${artifactId.toStr()}:*")
                    .flatMap { jedis.smembers(it) }
                    .mapNotNull { it.toComponentVersion() }
        }
    }

    override fun add(componentId: String, version: Version, artifacts: List<ArtifactId>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                artifacts.forEach { artifact ->
                    val componentKey = "$componentId:${version.version}"
                    val artifactKey = artifact.toStr()
                    pipe.sadd("libraries:$componentKey", artifactKey)
                    pipe.sadd("library:$artifactKey", componentKey)
                }
                pipe.sync()
            }
        }
    }

    override fun addArtifactId(artifactId: String?, version: Version) {
        changeArtifactId(artifactId, null, version)
    }

    override fun changeArtifactId(artifactId: String?, oldArtifactId: String?, version: Version?) {
        artifactId?.let {
            pool.resource.use { jedis ->
                val v = version?.version ?: "latest"
                jedis.sadd("library:$artifactId:$v", "")
            }
        }
    }

    private fun String.splitVersion(): Pair<String, String> {
        return this.lastIndexOf(':').let {
            idx -> this.substring(0, idx) to this.substring(idx + 1)
        }
    }

    private fun String.toComponentVersion(): ComponentVersion? {
        return this.takeIf { this.isNotBlank() }?.splitVersion()?.let { ComponentVersion(it.first, Version(it.second)) }
    }

}


