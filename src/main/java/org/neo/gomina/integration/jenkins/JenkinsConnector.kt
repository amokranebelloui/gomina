package org.neo.gomina.integration.jenkins

import org.neo.gomina.integration.jenkins.jenkins.BuildStatus

interface JenkinsConnector {
    fun getStatus(url: String): BuildStatus?
}