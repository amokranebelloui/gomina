package org.neo.gomina.model.dependency

import com.google.inject.Inject

interface InteractionsRepository {
    fun getAll(): List<Interactions>
    fun getFor(projectId: String): Interactions
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
    override fun getFor(projectId: String): Interactions {
        return getAll().merge().find { it.projectId == projectId } ?: Interactions(projectId)
    }
}

