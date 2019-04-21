package org.neo.gomina.integration.elasticsearch

import com.google.inject.assistedinject.Assisted
import org.apache.http.HttpHost
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ElasticEventsProviderConfig(
        var id: String,
        var host: String = "localhost",
        var port: Int = 9200,
        var timestamp: String = "timestamp",
        var type: String = "type",
        var message: String = "message",
        var envId: String = "envId",
        var instanceId: String = "instanceId",
        var version: String = "version"
) : EventsProviderConfig

class ElasticEventsProvider : EventsProvider {

    @Inject private lateinit var events: Events
    private val config: ElasticEventsProviderConfig
    private val client: RestHighLevelClient

    @Inject
    constructor(@Assisted config: ElasticEventsProviderConfig) {
        this.config = config
        this.client = RestHighLevelClient(RestClient.builder(HttpHost(config.host, config.port, "http")))
    }

    override fun name(): String = config.id

    override fun group(): String = "release"

    override fun reload(since: LocalDateTime) {
        val eventsToSave = try {
            val query = SearchRequest().source(SearchSourceBuilder()
                    .query(QueryBuilders.rangeQuery("timestamp").gte(since)))

            //val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val pattern = DateTimeFormatter.ISO_DATE_TIME
            client.search(query).hits.hits.mapNotNull {
                val map = it.sourceAsMap
                try {
                    Event(
                            "es-${it.id}",
                            timestamp = LocalDateTime.parse(map[config.timestamp] as String, pattern),
                            type = map[config.type] as String?,
                            message = map[config.message] as String?,
                            envId = map[config.envId] as String?,
                            instanceId = map[config.instanceId] as String?,
                            version = map[config.version] as String?
                    )
                }
                catch (e: Exception) {
                    null
                }
            }
        }
        catch (e: Exception) {
            val msg = "Cannot retrieve events from elastic search '${config.id}' '${config.host}:${config.port}'"
            logger.error(msg, e)
            throw Exception(msg)
        }
        events.save(eventsToSave, group())
    }

    companion object {
        private val logger = LogManager.getLogger(ElasticEventsProvider::class.java)
    }

}

