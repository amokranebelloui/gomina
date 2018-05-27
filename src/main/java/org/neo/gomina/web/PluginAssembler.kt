package org.neo.gomina.web

import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.ssh.SshPlugin
import javax.inject.Inject

class PluginAssembler {

    //@Inject lateinit var inventoryPlugin: InventoryPlugin
    //@Inject lateinit var jenkinsPlugin: JenkinsPlugin
    @Inject lateinit var scmPlugin: ScmPlugin
    @Inject lateinit var sshPlugin: SshPlugin
    //@Inject lateinit var sonarPlugin: SonarPlugin
    @Inject lateinit var monitoringPlugin: MonitoringPlugin

    fun init() {
        //inventoryPlugin.init()
        //jenkinsPlugin.init()
        scmPlugin.init()
        sshPlugin.init()
        //sonarPlugin.init()
        monitoringPlugin.init()
    }
}
