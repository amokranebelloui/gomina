package org.neo.gomina.model.dependency

import com.google.inject.Inject

interface InteractionsRepository {
    fun getAll(): List<Interactions>
    fun getFor(serviceId: String): Interactions
}

interface InteractionsProvider {
    fun getAll(): List<Interactions>
}

class ProviderBasedInteractionRepository : InteractionsRepository {

    @Inject lateinit var enrichDependencies: EnrichDependencies

    val providers = mutableListOf<InteractionsProvider>()

    override fun getAll(): List<Interactions> {
        val all = providers.flatMap { it.getAll() }
        val enriched = enrichDependencies.enrich(all)
        return (all + enriched).merge().toList()
    }
    override fun getFor(serviceId: String): Interactions {
        return getAll().merge().find { it.serviceId == serviceId } ?: Interactions(serviceId)
    }
}

