package org.neo.gomina.model.inventory

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
)

data class Instance(
        val id: String,
        val host: String?,
        val folder: String?
)

interface Inventory {
    fun getEnvironments(): Collection<Environment>
    fun getEnvironment(env: String): Environment?

    fun addEnvironment(id: String, type: String, description: String?, monitoringUrl: String?)
    fun updateEnvironment(id: String, type: String, description: String?, monitoringUrl: String?)
    fun deleteEnvironment(id: String)

    fun addService(env: String, svc: String, type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?)
    fun updateService(env: String, svc: String, type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?)
    fun renameService(env: String, svc: String)
    fun deleteService(env: String, svc: String)

    fun addInstance(env: String, svc: String, instanceId: String, host: String?, folder: String?)
    fun deleteInstance(env: String, svc: String, instanceId: String)

}
