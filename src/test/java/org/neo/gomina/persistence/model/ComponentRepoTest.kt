package org.neo.gomina.persistence.model

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.io.File
import kotlin.test.assertFailsWith

class ComponentRepoTest {

    @Test fun testJson() {
        val components = ComponentRepoFile().read(File("data/components.json"))
        components.forEach { println(it) }
        assertThat(components.size).isGreaterThan(0)
    }

    @Test fun testException() {
        assertFailsWith(RuntimeException::class) {
            ComponentRepoFile().read(File("data/components.txt"))
        }
    }
}

