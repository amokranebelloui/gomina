package org.neo.gomina.model.project.file

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.project.ProjectsReader
import java.io.File

class FileProjects : Projects {

    companion object {
        private val logger = LogManager.getLogger(FileProjects.javaClass)
    }

    private var file = File("data/projects.json")  // FIXME Configurable file
    private val reader = ProjectsReader()

    override fun getProjects(): List<Project> {
        return reader.read(file)
    }

    override fun getProject(projectId: String): Project? {
        return reader.read(file).find { it.id == projectId }
    }

}