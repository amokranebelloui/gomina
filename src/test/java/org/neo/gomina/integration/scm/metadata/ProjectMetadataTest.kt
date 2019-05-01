package org.neo.gomina.integration.scm.metadata

import org.apache.commons.io.IOUtils
import org.junit.Test
import java.io.File
import java.io.FileReader

class ProjectMetadataTest {
    @Test
    fun testMetadata() {
        val file = File("project.yaml")
        val metadataText = IOUtils.toString(FileReader(file))
        val metadata: ProjectMetadata = ProjectMetadataMapper().map(metadataText)

        println(metadata)
    }
}

