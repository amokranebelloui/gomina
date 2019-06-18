package org.neo.gomina.integration.sonar

import org.neo.gomina.integration.sonar.dummy.DummySonarConnector
import org.neo.gomina.integration.sonar.sonar.HttpSonarConnector
import javax.inject.Inject

data class SonarIndicators (
    var code: String,
    var loc: Double? = null,
    var coverage: Double? = null,
    var sonarUrl: String = ""
)

interface SonarConnector {

    fun getMetrics(): Map<String, SonarIndicators>
    fun getMetrics(resource: String?): Map<String, SonarIndicators>

}

class SonarConnectors {
    @Inject private lateinit var sonarConfig: SonarConfig

    fun getConnector(server: String): SonarConnector? {
        return sonarConfig.serverMap[server] ?. let {
            when (it.mode) {
                "dummy" -> DummySonarConnector()
                "http" -> HttpSonarConnector(it.url)
                else -> null
            }
        }
    }
}
