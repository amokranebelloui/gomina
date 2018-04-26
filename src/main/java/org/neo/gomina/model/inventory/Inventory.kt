package org.neo.gomina.model.inventory

data class Environment (
    val id: String,
    val name: String?,
    val type: String = "UNKNOWN",
    val monitoringUrl: String?,
    val active: Boolean = false,
    val services: List<Service> = emptyList()
)

data class Service (
    val svc: String,
    val type: String? = null,
    val project: String? = null,
    val instances: List<InvInstance> = emptyList()
)

data class InvInstance (
    val id: String,
    val host: String?,
    val folder: String?
)

interface Inventory {
    fun getEnvironments(): Collection<Environment>
    fun getEnvironment(env: String): Environment?
}
