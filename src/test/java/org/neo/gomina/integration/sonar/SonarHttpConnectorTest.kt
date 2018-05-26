package org.neo.gomina.integration.sonar

import org.junit.Test
import org.neo.gomina.integration.sonar.sonar.HttpSonarConnector

class HttpSonarConnectorTest {

    @Test
    @Throws(Exception::class)
    fun getMetrics() {
        val httpSonarConnector = HttpSonarConnector("http://localhost:9000")
        System.out.println(httpSonarConnector.getMetrics(null))
        System.out.println(httpSonarConnector.getMetrics("torkjell:torkjell"))
        System.out.println(httpSonarConnector.getMetrics("torkjell:unknown"))
    }

}