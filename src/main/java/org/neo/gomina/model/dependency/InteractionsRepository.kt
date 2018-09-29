package org.neo.gomina.model.dependency

interface InteractionsRepository {
    fun getAll(): List<Interactions>
}

interface InteractionsProvider {
    fun getAll(): List<Interactions>
}

class ProviderBasedInteractionRepository : InteractionsRepository {
    val providers = mutableListOf<InteractionsProvider>()
    override fun getAll(): List<Interactions> {
        return providers.flatMap { it.getAll() }
    }

}

