package org.neo.gomina.core.instances

interface InstancesExt {
    fun onGetInstances(env: String, instances: Instances)
    fun onReloadInstances(env: String) = Unit
    fun onRegisterForInstanceUpdates(listener: InstanceListener) = Unit
}