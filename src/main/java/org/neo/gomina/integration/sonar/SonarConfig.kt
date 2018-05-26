package org.neo.gomina.integration.sonar

import java.util.*

data class SonarServer(val id: String = "", val mode: String, val url: String = "")

data class SonarConfig (val servers: List<SonarServer> = ArrayList()) {
    val serverMap = servers.associateBy { it.id }
}