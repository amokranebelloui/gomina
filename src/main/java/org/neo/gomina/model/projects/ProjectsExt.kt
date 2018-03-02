package org.neo.gomina.model.projects

interface ProjectsExt {
    fun onGetProjects(projectSet: ProjectSet)
    fun onGetProject(projectId: String, projectDetail: ProjectDetail)
}