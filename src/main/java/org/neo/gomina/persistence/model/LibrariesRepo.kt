package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.model.dependency.ComponentVersion
import org.neo.gomina.model.dependency.Libraries
import org.neo.gomina.model.dependency.LibraryVersions
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
                host, port, 10000, null, 8)
        logger.info("Libraries Database connected $host $port")
    }

    override fun libraries(): List<LibraryVersions> {
        pool.resource.use { jedis ->
            return jedis.keys("library:*")
                    .map { it.replace(Regex("^library:"), "") }
                    //.map { it.splitVersion() }
                    .mapNotNull { Artifact.parse(it) }
                    .map { it to Version(it.version ?: "UNKNOWN") }
                    .map { (artifact, version) -> artifact.let { artifact.withoutVersion() to version } }
                    .groupBy( { it.first } ) { it.second }
                    .map { (artifact, versions) -> LibraryVersions(artifact, versions) }
        }
    }

    override fun library(artifact: Artifact): Map<Version, List<ComponentVersion>> {
        pool.resource.use { jedis ->
            return jedis.keys("library:$artifact:*")
                    .map { key -> key to Artifact.parse(key.replace(Regex("^library:"), "")) }
                    .filter { (_, a) ->
                        a?.withoutVersion() == artifact.withoutVersion()
                    }
                    .mapNotNull { (key, artifact) ->
                        val version = artifact?.getVersion()
                        val componentsVersions = jedis.smembers(key).mapNotNull { it.toComponentVersion() }
                        version?.let { version to componentsVersions }
                    }
                    .toMap()
        }
    }

    override fun forComponent(componentId: String, version: Version): List<Artifact> {
        pool.resource.use { jedis ->
            return jedis.keys("libraries:$componentId:${version.version}")
                    .flatMap { jedis.smembers(it) }
                    .mapNotNull { Artifact.parse(it) }
        }
    }

    override fun addArtifact(artifact: Artifact, version: Version) {
        pool.resource.use { jedis ->
            jedis.sadd("library:$artifact:${version.version}", "")
        }
    }

    override fun addUsage(componentId: String, version: Version, artifacts: List<Artifact>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                artifacts.forEach { artifact ->
                    val componentKey = "$componentId:${version.version}"
                    val artifactKey = artifact.toString()
                    pipe.sadd("libraries:$componentKey", artifactKey)
                    pipe.sadd("library:$artifactKey", componentKey)
                }
                pipe.sync()
            }
        }
    }

    override fun cleanSnapshotVersions(componentId: String) {
        pool.resource.use { jedis ->
            val snapshotComponentsV = jedis.keys("libraries:$componentId:*-SNAPSHOT")
            snapshotComponentsV.forEach { componentVersionKey ->
                val componentVersion = componentVersionKey.replace(Regex("^libraries:"), "")
                val libraries = jedis.smembers(componentVersionKey)
                libraries.forEach { library ->
                    jedis.srem("library:$library", componentVersion)
                    jedis.del(componentVersionKey)
                }
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


