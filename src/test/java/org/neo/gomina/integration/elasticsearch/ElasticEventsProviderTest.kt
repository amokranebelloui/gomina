package org.neo.gomina.integration.elasticsearch

import org.apache.http.HttpHost
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.junit.Test
import java.time.LocalDateTime

class ElasticEventsProviderTest {

    @Test
    fun testElasticSearch() {
        println(LocalDateTime.now())
        val client = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))

        val query1 = SearchRequest("delivery").source(SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(1000)
        )
        val query2 = SearchRequest().source(SearchSourceBuilder()
                .query(QueryBuilders.rangeQuery("timestamp").gte("2018-06-02T00:03Z")))
        val query3 = SearchRequest("delivery").source(SearchSourceBuilder()
                .query(QueryBuilders.rangeQuery("@timestamp").gte(LocalDateTime.of(2019, 5, 20, 18, 3, 51)))
                .size(1000)
        )

        val result = client.search(query1)
        println("Results ${result.hits.totalHits}")
        result.hits.hits.forEach {
            val map = it.sourceAsMap
            println("-> " + map)
            //("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        }

        client.close()
    }

}