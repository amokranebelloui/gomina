package org.neo.gomina.integration.jenkins

import org.junit.Test
import org.neo.gomina.integration.jenkins.jenkins.JenkinsConnectorImpl
import java.util.*

class JenkinsConnectorImplTest {

    @Test
    fun testJenkinsConnector() {
        //val url = "https://builds.apache.org/job/kafka-1.1-jdk7/"
        val url = "http://localhost:8081/job/gomina/"
        val status = JenkinsConnectorImpl().getStatus(url)
        println(status)
        status?.let { println(Date(it.timestamp)) }
    }

}