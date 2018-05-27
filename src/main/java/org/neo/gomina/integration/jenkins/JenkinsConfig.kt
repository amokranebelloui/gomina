package org.neo.gomina.integration.jenkins

import java.util.*


data class JenkinsServer(val id: String = "", val location: String = "")

data class JenkinsConfig(val servers: List<JenkinsServer> = ArrayList()) {
    val serverMap = servers.associateBy { it.id }
}
