package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import java.io.File

class ComponentRepoFile : ComponentRepo, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(ComponentRepoFile.javaClass)
    }

    @Inject @Named("components.file")
    private lateinit var file: File

    fun read(file: File): List<Component> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Component> = read(file)

    override fun get(componentId: String): Component? = read(file).find { it.id == componentId }

}
