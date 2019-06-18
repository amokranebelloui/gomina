package org.neo.gomina.integration.sonar.sonar

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.sonar.SonarConnector
import org.neo.gomina.integration.sonar.SonarIndicators
import java.util.*

class HttpSonarConnector(val url: String) : SonarConnector {

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun getMetrics(): Map<String, SonarIndicators> {
        logger.info("Get metrics for $url")
        return getMetrics(null)
    }

    override fun getMetrics(resource: String?): Map<String, SonarIndicators> {
        try {
            val httpclient = HttpClients.createDefault()
            val httpGet = query2(resource)
            val response = httpclient.execute(httpGet)
            response.use {
                logger.info("-> Result " + it.statusLine)
                if (it.statusLine.statusCode == 200) {
                    val entity = it.entity
                    val map = readResponse2(resource ?: "", entity)
                    EntityUtils.consume(entity)
                    return map
                }
            }
        } catch (e: Exception) {
            logger.error("", e)
        }

        return emptyMap()
    }

    private fun query1(resource: String?): HttpGet {
        val resourceQuery = if (StringUtils.isNotBlank(resource)) "resource=$resource&" else ""
        return HttpGet(url + "/api/resources?" + resourceQuery + "metrics=ncloc,coverage")
    }

    private fun readResponse1(entity: HttpEntity): HashMap<String, SonarIndicators> {
        val map = HashMap<String, SonarIndicators>()
        val data = mapper.readValue<List<Map<String, Object>>>(entity.content)
        for (comp in data) {
            val key = comp.get("key") as String
            val msr = comp.get("msr") as List<Map<String, String>>?
            val ncloc = getMetric(msr, "ncloc") as Double?
            val coverage = getMetric(msr, "coverage") as Double?
            logger.info("-> Data $key $ncloc $coverage")
            val indicators = SonarIndicators(key, ncloc, coverage)
            map[key] = indicators
        }
        return map
    }

    private fun query2(resource: String?): HttpGet {
        val resourceQuery = if (StringUtils.isNotBlank(resource)) "componentKey=$resource&" else ""
        return HttpGet(url + "/api/measures/component?" + resourceQuery + "metricKeys=ncloc,coverage")
    }

    data class Metric(val metric: String, val value: String)
    data class Metrics(val id: String, val measures: List<Metric>)
    data class ComponentMetrics(val component: Metrics)

    private fun readResponse2(key: String, entity: HttpEntity): Map<String, SonarIndicators> {
        val map = HashMap<String, SonarIndicators>()
        val data = mapper.readValue<ComponentMetrics>(entity.content)
        logger.info("-> Data $data")
        val indicatorMap = data.component.measures.associate { it.metric to it.value }
        val indicators = SonarIndicators(key, indicatorMap["ncloc"]?.toDouble(), indicatorMap["coverage"]?.toDouble())
        map[key] = indicators
        return map
    }

    private fun getMetric(msr: List<Map<String, String>>?, metric: String?): Any? {
        if (msr != null) {
            for (m in msr) {
                if (metric != null && metric == m["key"]) {
                    return m["val"]
                }
            }
        }
        return null
    }

    companion object {
        private val logger = LogManager.getLogger(HttpSonarConnector::class.java)
    }

}