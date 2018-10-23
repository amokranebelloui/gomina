package org.neo.gomina.persistence.model

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class ComponentRepoFileTest {

    @Test fun getComponents() {
        val components = ComponentRepoFile().getAll()
        assertThat(components.size).isGreaterThan(3)
    }

    @Test fun getComponent() {
        val component = ComponentRepoFile().get("kernel")
        assertThat(component?.label).isEqualTo("Kernel")
    }

}