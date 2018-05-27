package org.neo.gomina.persistence.model

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import java.io.File

class FileProjects : Projects {

    companion object {
        private val logger = LogManager.getLogger(FileProjects.javaClass)
    }

    @Inject @Named("projects.file")
    private lateinit var file: File
    
    private val reader = ProjectsReader()

    override fun getProjects(): List<Project> {
        return reader.read(file)
    }

    override fun getProject(projectId: String): Project? {
        return reader.read(file).find { it.id == projectId }
    }

}

class ProjectsReader {
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val jsonMapper = ObjectMapper(JsonFactory()).registerKotlinModule()
    constructor () {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        jsonMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }
    fun read(file: File): List<Project> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }
}