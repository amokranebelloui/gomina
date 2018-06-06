package org.neo.gomina.web

import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import javax.inject.Inject

class PluginAssembler {

    @Inject lateinit var monitoringPlugin: MonitoringPlugin

    fun init() {
        monitoringPlugin.prepare()
        monitoringPlugin.init()
    }
}
