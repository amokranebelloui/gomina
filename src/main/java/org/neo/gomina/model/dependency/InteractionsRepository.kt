package org.neo.gomina.model.dependency

interface InteractionsRepository {
    fun getAll(): List<Interactions>
}