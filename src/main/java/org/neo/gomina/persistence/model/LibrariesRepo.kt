package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.model.dependency.ComponentVersion
import org.neo.gomina.model.dependency.Libraries
import org.neo.gomina.model.dependency.LibraryVersions
import org.neo.gomina.model.dependency.VersionUsage
import org.neo.gomina.model.version.Version
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Response

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
        data class LibraryData(val artifactId: Artifact, val count: Response<Long>, val containsBlank: Response<Boolean>, val dismissed: Response<String>?)
        pool.resource.use { jedis ->
            val pipe1 = jedis.pipelined()
            val libs = jedis.keys("library:*")
                    .mapNotNull { key ->
                        Pair(key.replace(Regex("^library:"), ""), pipe1.hget(key, "dismissed"))
                    }
                    .toMap()
            pipe1.sync()
            val pipe2 = jedis.pipelined()
            val data = jedis.keys("library_usage:*")
                    .mapNotNull { key ->
                        Artifact.parse(key.replace(Regex("^library_usage:"), ""))?.let {
                            LibraryData(it, pipe2.scard(key), pipe2.sismember(key, ""), libs[key.replace(Regex("^library_usage:"), "")])
                        }
                    }
            pipe2.sync()

            val result = data
                    .map { (artifact, nb, containsBlank, dismissed) ->
                        val count = nb.get() - if (containsBlank.get()) 1 else 0
                        val isDismissed = dismissed?.get()?.toBoolean() ?: false
                        artifact to VersionUsage(Version(artifact.version ?: "UNKNOWN", dismissed = isDismissed), count.toInt())
                    }
                    .map { (artifact, versionUsage) -> artifact.withoutVersion() to versionUsage }
                    .groupBy({ it.first }) { it.second }
                    .map { (artifact, versions) -> LibraryVersions(artifact, versions) }
            return result
        }
    }

    override fun library(artifact: Artifact): Map<Version, List<ComponentVersion>> {
        pool.resource.use { jedis ->
            val pipe1 = jedis.pipelined()
            val libs = jedis.keys("library:*")
                    .mapNotNull { key ->
                        Pair(key.replace(Regex("^library:"), ""), pipe1.hget(key, "dismissed"))
                    }
                    .toMap()
            pipe1.sync()

            val pipe = jedis.pipelined()
            val usage = jedis.keys("library_usage:$artifact:*")
                    .map { key -> key to Artifact.parse(key.replace(Regex("^library_usage:"), "")) }
                    .filter { (_, a) ->
                        a?.withoutVersion() == artifact.withoutVersion()
                    }
                    .mapNotNull { (key, artifact) ->
                        val isDismissed = libs[key.replace(Regex("^library_usage:"), "")]?.get()?.toBoolean() ?: false
                        val version = artifact?.getVersion()?.let { Version(it.version, it.revision, isDismissed) }
                        val componentsVersions = pipe.smembers(key)
                        version?.let { version to componentsVersions }
                    }
            pipe.sync()
            val result = usage
                    .map { (version, componentsFutures) ->
                        version to componentsFutures.get().mapNotNull { it.toComponentVersion() }
                    }
                    .toMap()
            return result
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
            jedis.hsetnx("library:${artifact.withoutVersion()}:${version.version}", "dismissed", "false")
            jedis.sadd("library_usage:${artifact.withoutVersion()}:${version.version}", "")
        }
    }

    override fun addUsage(componentId: String, version: Version, artifacts: List<Artifact>) {
        pool.resource.use { jedis ->
            jedis.pipelined().let { pipe ->
                artifacts.forEach { artifact ->
                    val componentKey = "$componentId:${version.version}"
                    val artifactKey = artifact.toString()
                    pipe.hsetnx("library:$artifactKey", "dismissed", "false")
                    pipe.sadd("library_usage:$artifactKey", componentKey)
                    pipe.sadd("libraries:$componentKey", artifactKey)
                }
                pipe.sync()
            }
        }
    }

    override fun dismissSnapshotVersion(artifact: Artifact, version: Version) {
        pool.resource.use { jedis ->
            jedis.hset("library:${artifact.withoutVersion()}:${version.version}", "dismissed", "true")
            /*
            jedis.keys("libraries:$componentId:*-SNAPSHOT").forEach { key ->
                val componentVersion = key.replace(Regex("^libraries:"), "")
                val libraries = jedis.smembers(key)
                libraries.forEach { library ->
                    //jedis.srem("library_usage:$library", componentVersion)
                    //jedis.del(key)
                }
            }
            */
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


