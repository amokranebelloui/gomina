package org.neo.gomina.model.inventory

data class Environment(
        val id: String,
        val name: String?,
        val type: String = "UNKNOWN",
        val monitoringUrl: String?,
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
        val activeCount: Int = 1,
        val project: String? = null,
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
}
