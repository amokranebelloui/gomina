@file:JvmName("ScmPluginKt")

package org.neo.gomina.plugins.scm

import com.thoughtworks.xstream.XStream
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.scm.Commit
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class ScmCache {

    companion object {
        private val logger = LogManager.getLogger(ScmCache::class.java)
    }

    private val xStream = XStream()
    private val cache = HashMap<String, ScmDetails>()

    fun getDetail(svnRepo: String, svnUrl: String): ScmDetails? {
        val detailCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val detailCacheFile = File(".cache/" + detailCacheFileName)

        if (cache.containsKey(svnUrl)) {
            val scmDetails = cache[svnUrl]
            logger.debug("SCM Detail Served from Memory Cache $scmDetails")
            return scmDetails
        }
        else if (detailCacheFile.exists()) {
            try {
                val scmDetails = xStream.fromXML(detailCacheFile) as ScmDetails
                cache.put(svnUrl, scmDetails)
                logger.debug("SCM Detail Served from File Cache $scmDetails")
                return scmDetails
            }
            catch (e: Exception) {
                logger.debug("Error loading Cache", e)
                logger.info("Error loading Cache from File $detailCacheFile")
            }
        }
        return null
    }

    fun cacheDetail(svnRepo: String, svnUrl: String, scmDetails: ScmDetails) {
        val detailCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/" + detailCacheFileName)

        cache.put(svnUrl, scmDetails)
        try {
            xStream.toXML(scmDetails, FileOutputStream(cacheFile))
        } catch (e: FileNotFoundException) {
            logger.error("Saving cache for $cacheFile", e)
        }
    }

    fun getLog(svnRepo: String, svnUrl: String): List<Commit> {
        val logCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/$logCacheFileName.log")
        return if (cacheFile.exists()) xStream.fromXML(cacheFile) as List<Commit> else emptyList()
    }

    fun cacheLog(svnRepo: String, svnUrl: String, result: List<Commit>) {
        val logCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/$logCacheFileName.log")

        try {
            xStream.toXML(result, FileOutputStream(cacheFile))
        } catch (e: Exception) {
            logger.error("Cannot save cache in $cacheFile")
        }
    }

}

