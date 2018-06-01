package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import java.io.File

class ProjectsFile : Projects, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(ProjectsFile.javaClass)
    }

    @Inject @Named("projects.file")
    private lateinit var file: File

    fun read(file: File): List<Project> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getProjects(): List<Project> = read(file)

    override fun getProject(projectId: String): Project? = read(file).find { it.id == projectId }

}
