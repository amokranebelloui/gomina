package org.neo.gomina.model.dependency

interface InteractionsRepository {
    fun getAll(): List<Interactions>
    fun update(source: String, interactions: List<Interactions>)
}

interface InteractionsProvider {
    fun name(): String
    fun getAll(): List<Interactions>
}

class InteractionProviders {
    val providers = mutableListOf<InteractionsProvider>()
}

