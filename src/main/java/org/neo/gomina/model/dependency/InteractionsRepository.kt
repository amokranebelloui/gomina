package org.neo.gomina.model.dependency

interface InteractionsRepository {
    fun getAll(): List<Interactions>
    fun update(source: String, interactions: List<Interactions>)
    fun getApi(componentId: String): List<Function>
    fun addApi(componentId: String, function: Function)
    fun removeApi(componentId: String, name: String)
    fun getUsage(componentId: String): List<FunctionUsage>
    fun addUsage(componentId: String, functionUsage: FunctionUsage)
    fun removeUsage(componentId: String, name: String)
}

interface InteractionsProvider {
    fun name(): String
    fun getAll(): List<Interactions>
}

class InteractionProviders {
    val providers = mutableListOf<InteractionsProvider>()
}

