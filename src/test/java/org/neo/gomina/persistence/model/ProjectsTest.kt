package org.neo.gomina.persistence.model

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.io.File
import kotlin.test.assertFailsWith

class ProjectsTest {

    @Test fun testYaml() {
        val projects = ProjectsFile().read(File("data/projects.yaml"))
        projects.forEach { println(it) }
        assertThat(projects.size).isGreaterThan(0)
    }

    @Test fun testJson() {
        val projects = ProjectsFile().read(File("data/projects.json"))
        projects.forEach { println(it) }
        assertThat(projects.size).isGreaterThan(0)
    }

    @Test fun testException() {
        assertFailsWith(RuntimeException::class) {
            ProjectsFile().read(File("data/projects.txt"))
        }
    }
}

