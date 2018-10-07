package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import java.io.File

class WorkListFile : WorkList, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(WorkListFile.javaClass)
    }

    @Inject
    @Named("work.file")
    private lateinit var file: File

    fun read(file: File): List<Work> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Work> = read(file)

}
