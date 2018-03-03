package org.neo.gomina.plugins.sonar

import org.junit.Test
import org.neo.gomina.plugins.sonar.connectors.HttpSonarConnector

class HttpSonarConnectorTest {

    @Test
    @Throws(Exception::class)
    fun getMetrics() {
        val httpSonarConnector = HttpSonarConnector()
        httpSonarConnector.url = "http://localhost:9000"
        System.out.println(httpSonarConnector.getMetrics(null))
        System.out.println(httpSonarConnector.getMetrics("torkjell:torkjell"))
        System.out.println(httpSonarConnector.getMetrics("torkjell:unknown"))
    }

}