package org.neo.gomina.persistence.model

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class ProjectsFileTest {

    @Test fun getProjects() {
        val projects = ProjectsFile().getProjects()
        assertThat(projects.size).isGreaterThan(3)
    }

    @Test fun getProject() {
        val project = ProjectsFile().getProject("kernel")
        assertThat(project?.label).isEqualTo("Kernel")
    }

}