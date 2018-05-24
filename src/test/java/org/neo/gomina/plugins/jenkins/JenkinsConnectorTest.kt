package org.neo.gomina.plugins.jenkins

import org.junit.Test
import java.util.*

class JenkinsConnectorTest {

    @Test
    fun testJenkinsConnector() {
        val status = JenkinsConnector().getStatus("https://builds.apache.org/job/kafka-1.1-jdk7/")
        println(status)
        status?.let { println(Date(it.timestamp)) }
    }

}