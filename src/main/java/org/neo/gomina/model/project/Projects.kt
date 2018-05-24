package org.neo.gomina.model.project

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

data class Project (
    var id: String,
    var label: String?,
    var type: String?,
    var tags: String?,
    var svnRepo: String = "",
    var svnUrl: String = "",
    var maven: String?,
    var sonarServer: String = "",
    var jenkinsServer: String = "",
    var jenkinsJob: String?
)

interface Projects {
    fun getProjects(): List<Project>
    fun getProject(projectId: String): Project?
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


