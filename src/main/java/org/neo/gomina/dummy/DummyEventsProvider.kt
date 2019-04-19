package org.neo.gomina.dummy

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.assistedinject.Assisted
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DummyEventsProviderConfig(
        var id: String,
        var file: String
) : EventsProviderConfig

class DummyEventsProvider : EventsProvider {

    @Inject private lateinit var components: ComponentRepo

    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
            .registerModule(JodaModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    val config: DummyEventsProviderConfig
    val file: File

    @Inject
    constructor(@Assisted config: DummyEventsProviderConfig) {
        this.file = File(config.file)
        this.config = config
    }

    override fun name() = config.id

    override fun events(since: LocalDateTime): List<Event> {
        val map = components.getAll().associateBy { it.maven }
        val events = mapper.readValue<List<DummyEvent>>(file).map {
            Event(
                    id = "${it.timestamp}-dummy",
                    timestamp = LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_DATE_TIME),
                    type = it.type,
                    message = it.message,
                    envId = it.envId,
                    instanceId = it.instanceId,
                    componentId = it.component?.let { map[it]?.id },
                    version = it.version
            )
        }
        return events.filter { it.timestamp > since }
    }

}

private data class DummyEvent (
        val id: String,
        val timestamp: String,
        val type: String?,
        val message: String?,
        val envId: String? = null,
        val instanceId: String? = null,
        val component: String? = null,
        val version: String? = null
)
