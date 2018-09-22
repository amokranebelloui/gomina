package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.ProjectDeps
import org.neo.gomina.model.dependency.ProjectsDeps
import java.io.File

class ProjectsDepsFile : ProjectsDeps, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(ProjectsDepsFile.javaClass)
    }

    @Inject
    @Named("projects.deps.file")
    private lateinit var file: File

    fun read(file: File): List<ProjectDeps> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<ProjectDeps> {
        return read(file)
    }
}