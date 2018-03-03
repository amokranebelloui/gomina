package org.neo.gomina.plugins.sonar

class SonarIndicators (
    var code: String,
    var loc: Double? = null,
    var coverage: Double? = null
)

interface SonarConnector {

    fun getMetrics(): Map<String, SonarIndicators>
    fun getMetrics(resource: String?): Map<String, SonarIndicators>

}