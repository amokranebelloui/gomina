package org.neo.gomina.utils

import com.thoughtworks.xstream.XStream
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class Cache<T>(val prefix:String, val fixFunction:(T) -> Unit = {}) {
    
    private val xStream = XStream()
    private val cache = HashMap<String, T>()

    init {
        File(".cache/$prefix").
                takeUnless { it.exists() }?.
                let { logger.info("$prefix: Created $it ${it.mkdirs()}") }
    }

    fun get(id: String): T? = getOrLoad(id) {null}

    fun get(id: String, fromCache: Boolean, retrieve:(String) -> T? = { null }): T? {
        return if (fromCache) {
            this.getOrLoad(id, retrieve)
        }
        else {
            logger.info("$prefix: Retrieving Data from Underlying Service, $id")
            retrieve(id)?.also { data -> this.cache(id, data) }
        }
    }

    fun getOrLoad(id: String, retrieve:(String) -> T? = { null }): T? {
        val file = fileNameFor(id)

        if (cache.containsKey(id)) {
            val data = cache[id]
            logger.debug("$prefix: Data Served from Memory Cache $data")
            return data
        }
        else if (file.exists()) {
            try {
                val data = xStream.fromXML(file) as T
                fixFunction(data) // Keep  the defaulting, as it gets set to null if not in the file
                cache.put(id, data)
                logger.debug("$prefix: Data Served from File Cache $data")
                return data
            }
            catch (e: Exception) {
                logger.debug("$prefix: Error loading Cache", e)
                logger.info("$prefix: Error loading Cache from File $file")
            }
        }
        logger.info("$prefix: Cache miss $id")
        return retrieve(id) ?. also { data -> cache(id, data) }
    }

    fun cache(id: String, data: T) {
        val file = fileNameFor(id)

        cache.put(id, data)
        try {
            xStream.toXML(data, FileOutputStream(file))
        }
        catch (e: FileNotFoundException) {
            logger.error("Saving cache for $file", e)
        }
    }

    private fun fileNameFor(id: String): File {
        val filename = id.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        return File(".cache/$prefix/$filename.$prefix")
    }

    companion object {
        private val logger = LogManager.getLogger(Cache::class.java)
    }

}