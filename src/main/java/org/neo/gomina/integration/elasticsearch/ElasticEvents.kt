package org.neo.gomina.integration.elasticsearch

import com.google.inject.name.Named
import org.apache.http.HttpHost
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class ElasticEvents : EventsProvider {


    @Inject @Named("elastic.host") lateinit var host: String
    @Inject @Named("elastic.port") var port: Int = 9200

    private lateinit var client: RestHighLevelClient

    @Inject
    fun init() {
        client = RestHighLevelClient(RestClient.builder(HttpHost(host, port, "http")))
    }

    override fun getEvents(since: LocalDateTime): List<Event> {
        try {
            val query = SearchRequest().source(SearchSourceBuilder()
                    .query(QueryBuilders.rangeQuery("timestamp").gte(since)))

            //val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val pattern = DateTimeFormatter.ISO_DATE_TIME
            return client.search(query).hits.hits.mapNotNull {
                val map = it.sourceAsMap
                try {
                    Event(
                            LocalDateTime.parse(map["timestamp"] as String, pattern),
                            map["type"] as String?,
                            map["message"] as String?,
                            map["env"] as String?,
                            map["instance"] as String?,
                            map["version"] as String?
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Cannot retrieve events from elastic search $host:$port", e)
            return emptyList()
        }
    }

    companion object {
        private val logger = LogManager.getLogger(ElasticEvents::class.java)
    }

}

