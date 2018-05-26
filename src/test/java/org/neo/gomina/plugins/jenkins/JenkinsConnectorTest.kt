package org.neo.gomina.plugins.jenkins

import org.junit.Test
import java.util.*

class JenkinsConnectorTest {

    @Test
    fun testJenkinsConnector() {
        //val url = "https://builds.apache.org/job/kafka-1.1-jdk7/"
        val url = "http://localhost:8081/job/gomina/"
        val status = JenkinsConnector().getStatus(url)
        println(status)
        status?.let { println(Date(it.timestamp)) }
    }

}