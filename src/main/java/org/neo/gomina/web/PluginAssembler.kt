package org.neo.gomina.web

import org.neo.gomina.plugins.jenkins.JenkinsPlugin
import org.neo.gomina.plugins.project.ProjectPlugin
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.sonar.SonarPlugin
import javax.inject.Inject

class PluginAssembler {

    @Inject lateinit var projectPlugin: ProjectPlugin
    @Inject lateinit var jenkinsPlugin: JenkinsPlugin
    @Inject lateinit var scmPlugin: ScmPlugin
    @Inject lateinit var sonarPlugin: SonarPlugin

    fun init() {
        projectPlugin.init()
        jenkinsPlugin.init()
        scmPlugin.init()
        sonarPlugin.init()
    }
}
