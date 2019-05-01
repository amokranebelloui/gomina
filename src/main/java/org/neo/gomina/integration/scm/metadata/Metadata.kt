package org.neo.gomina.integration.scm.metadata

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.LocalDate

data class ProjectMetadata(
        var id: String?,
        var label: String?,
        var owner: String?,
        var inceptionDate: LocalDate?,
        var criticity: Int?,

        var type: String? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),

        var api: List<ApiMetadata> = emptyList(),
        var dependencies: List<DepnedencyMetadata> = emptyList()

)

data class ApiMetadata(var name: String, var type: String)

data class DepnedencyMetadata(var name: String, var type: String, val usage: String? = null)

class ProjectMetadataMapper {
    private val yamlMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    fun map(content: String): ProjectMetadata = yamlMapper.readValue(content)
}
