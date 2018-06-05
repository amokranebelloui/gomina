package org.neo.gomina.web

import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import org.neo.gomina.plugins.ssh.SshPlugin
import javax.inject.Inject

class PluginAssembler {

    @Inject lateinit var sshPlugin: SshPlugin
    @Inject lateinit var monitoringPlugin: MonitoringPlugin

    fun init() {
        sshPlugin.init()
        monitoringPlugin.init()
    }
}
