package org.neo.gomina.plugins.ssh

import com.thoughtworks.xstream.XStream
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class SshCache {

    companion object {
        private val logger = LogManager.getLogger(SshCache::class.java)
    }

    private val xStream = XStream()
    private val cache = HashMap<String, SshDetails>()

    fun getDetail(host: String, folder: String): SshDetails? {
        val key = host + "-" + folder.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/ssh-$key")
        return when {
            cache.containsKey(key) -> cache[key]
            cacheFile.exists() -> try {
                    val sshDetails = xStream.fromXML(cacheFile) as SshDetails
                    cache.put(key, sshDetails)
                    logger.debug("SSH Detail Served from File Cache $sshDetails")
                    sshDetails
                }
                catch (e: Exception) {
                    logger.debug("Error loading Cache", e)
                    logger.info("Error loading Cache from File $cacheFile")
                    null
                }
            else -> null
        }
    }

    fun cacheDetail(host: String, folder: String, sshDetails: SshDetails) {
        val key = host + "-" + folder.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/ssh-$key")

        cache.put(key, sshDetails)
        try {
            xStream.toXML(sshDetails, FileOutputStream(cacheFile))
        } catch (e: FileNotFoundException) {
            logger.error("Saving cache for $cacheFile", e)
        }
    }

}