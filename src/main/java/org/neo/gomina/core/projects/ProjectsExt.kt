package org.neo.gomina.core.projects

interface ProjectsExt {
    fun onGetProjects(projectSet: ProjectSet)
    fun onGetProject(projectId: String, projectDetail: ProjectDetail)
    fun onReloadProject(projectId: String) = Unit
}