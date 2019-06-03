package org.neo.gomina.integration.elasticsearch

import com.google.inject.assistedinject.Assisted
import org.apache.http.HttpHost
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.event.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ElasticEventsProviderConfig(
        var id: String,
        var host: String = "localhost",
        var port: Int = 9200,
        var indice: String?,
        var size: Int = 1000,
        var timestamp: String = "timestamp",
        var type: String = "type",
        var message: String = "message",
        var envId: String = "envId",
        var instanceId: String = "instanceId",
        var componentId: String = "componentId",
        var version: String = "version"
) : EventsProviderConfig

class ElasticEventsProvider : EventsProvider {

    @Inject private lateinit var components: ComponentRepo
    @Inject private lateinit var events: Events
    private val config: ElasticEventsProviderConfig
    private val client: RestHighLevelClient

    @Inject
    constructor(@Assisted config: ElasticEventsProviderConfig) {
        this.config = config
        this.client = RestHighLevelClient(RestClient.builder(HttpHost(config.host, config.port, "http")))
    }

    override fun name(): String = config.id

    override fun reload(since: LocalDateTime) {
        logger.info("Loading ES events from ${config.indice} since: $since maxResults: ${config.size}")
        val componentMap = components.getAll().map { it.artifactId to it.id }.toMap()
        val eventsToSave = try {
            val request = config.indice?.let { SearchRequest(it) } ?: SearchRequest()
            val query = request
                    .source(SearchSourceBuilder()
                        //.query(QueryBuilders.matchAllQuery())
                        .query(QueryBuilders.rangeQuery(config.timestamp).gte(since))
                        .size(config.size)
                    )

            //val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val pattern = DateTimeFormatter.ISO_DATE_TIME
            client.search(query).hits.hits.mapNotNull {
                val map = it.sourceAsMap
                try {
                    val timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(map[config.timestamp] as Long), ZoneOffset.UTC)
                    Event(
                            "es-${it.id}",
                            //timestamp = LocalDateTime.parse(map[config.timestamp] as String, pattern),
                            timestamp = timestamp,
                            group = EventCategory.RELEASE,
                            type = config.type,
                            message = map[config.message] as String?,
                            envId = map[config.envId] as String?,
                            instanceId = map[config.instanceId] as String?,
                            componentId = (map[config.componentId] as String?)?.let { artifact -> componentMap[artifact] },
                            version = map[config.version] as String?
                    )
                }
                catch (e: Exception) {
                    logger.error("Cannot process event", e)
                    null
                }
            }
        }
        catch (e: Exception) {
            val msg = "Cannot retrieve events from elastic search '${config.id}' '${config.host}:${config.port}'"
            logger.error(msg, e)
            throw Exception(msg)
        }
        logger.info("${eventsToSave.size} events found")
        events.save(eventsToSave)
    }

    companion object {
        private val logger = LogManager.getLogger(ElasticEventsProvider::class.java)
    }

}

