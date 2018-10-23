package org.neo.gomina.integration.nexus

import org.junit.Test

class NexusConnectorTest {

    @Test
    fun testGetContent() {
        val nexusConnector = NexusConnector("localhost", 8082, releaseRepo = "amokrane", isNexus3 = false)
        val content = nexusConnector.getContent("org.neo.test", "test-photo", "0.0.1", type = "pom")
        println("Content: $content")
    }
}
