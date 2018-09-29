package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.Interactions
import org.neo.gomina.model.dependency.InteractionsProvider
import org.neo.gomina.model.dependency.ProviderBasedInteractionRepository
import java.io.File

class InteractionsFileProvider : InteractionsProvider, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(InteractionsFileProvider.javaClass)
    }

    @Inject @Named("interactions.file") private lateinit var file: File
    @Inject private lateinit var repository: ProviderBasedInteractionRepository

    @Inject fun init() {
        repository.providers.add(this)
    }
    
    fun read(file: File): List<Interactions> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Interactions> {
        return read(file)
    }
}