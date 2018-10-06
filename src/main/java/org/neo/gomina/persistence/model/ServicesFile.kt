package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.service.Service
import org.neo.gomina.model.service.Services
import java.io.File

class ServicesFile : Services, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(ServicesFile.javaClass)
    }

    @Inject @Named("services.file")
    private lateinit var file: File

    fun read(file: File): List<Service> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getServices(): List<Service> = read(file)

}
