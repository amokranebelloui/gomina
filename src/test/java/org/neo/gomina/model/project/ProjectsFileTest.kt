package org.neo.gomina.model.project

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.neo.gomina.model.project.file.FileProjects

class FileProjectsTest {

    @Test fun getProjects() {
        val projects = FileProjects().getProjects()
        assertThat(projects.size).isGreaterThan(3)
    }

    @Test fun getProject() {
        val project = FileProjects().getProject("kernel")
        assertThat(project?.label).isEqualTo("Kernel")
    }

}