package org.neo.gomina.model.inventory

import org.neo.gomina.model.version.Version

data class Environment(
        val id: String,
        val type: String = "UNKNOWN", // PROD TEST
        val name: String? = null, // FIXME Refactor to description
        val monitoringUrl: String? = null,
        val active: Boolean = false,
        val services: List<Service> = emptyList()
)

enum class ServiceMode {
    ONE_ONLY,
    LEADERSHIP,
    LOAD_BALANCING,
    OFFLINE,
}

data class Service(
        val svc: String,
        val type: String? = null,
        val mode: ServiceMode? = ServiceMode.ONE_ONLY,
        val activeCount: Int? = 1,
        val componentId: String? = null,
        val instances: List<Instance> = emptyList()
) {
    companion object {
        fun safe(service: String?) = service ?: "x"
    }
}

data class Instance(
        val id: String,
        val host: String?,
        val folder: String?,

        var deployedVersion: Version? = null,
        var confRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null
)

interface Inventory {
    fun getEnvironments(): Collection<Environment>
    fun getEnvironment(env: String): Environment?

    fun addEnvironment(id: String, type: String, description: String?, monitoringUrl: String?)
    fun updateEnvironment(id: String, type: String, description: String?, monitoringUrl: String?)
    fun deleteEnvironment(id: String)

    fun addService(env: String, svc: String,
                   type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?)
    fun updateService(env: String, svc: String,
                      type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?)
    fun renameService(env: String, svc: String, newSvc: String)
    fun deleteService(env: String, svc: String)

    fun addInstance(env: String, svc: String, instanceId: String, host: String?, folder: String?)
    fun deleteInstance(env: String, svc: String, instanceId: String)

    fun updateDeployedRevision(env: String, svc: String, instanceId: String, version: Version?)
    fun updateConfigStatus(env: String, svc: String, instanceId: String,
                           confRevision: String?, confCommitted: Boolean?, confUpToDate: Boolean?)
}
