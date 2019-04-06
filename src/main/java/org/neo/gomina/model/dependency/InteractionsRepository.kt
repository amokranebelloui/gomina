package org.neo.gomina.model.dependency

interface InteractionsRepository {
    fun getAll(): List<Interactions>
    fun getFor(serviceId: String): Interactions
}

interface InteractionsProvider {
    fun getAll(): List<Interactions>
}

class InteractionProviders {
    val providers = mutableListOf<InteractionsProvider>()
}

