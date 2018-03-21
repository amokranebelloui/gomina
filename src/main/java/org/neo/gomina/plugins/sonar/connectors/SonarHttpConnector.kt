package org.neo.gomina.plugins.sonar.connectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.plugins.sonar.SonarConnector
import org.neo.gomina.plugins.sonar.SonarIndicators
import java.io.IOException
import java.util.*

class HttpSonarConnector(val url: String) : SonarConnector {

    private val mapper = ObjectMapper()

    override fun getMetrics(): Map<String, SonarIndicators> {
        logger.info("Get metrics for $url")
        return getMetrics(null)
    }

    override fun getMetrics(resource: String?): Map<String, SonarIndicators> {
        val map = HashMap<String, SonarIndicators>()
        try {
            val httpclient = HttpClients.createDefault()
            val resourceQuery = if (StringUtils.isNotBlank(resource)) "resource=$resource&" else ""
            val httpGet = HttpGet(url + "/api/resources?" + resourceQuery + "metrics=ncloc,coverage")
            val response1 = httpclient.execute(httpGet)
            try {
                logger.info("-> Result " + response1.statusLine)
                if (response1.statusLine.statusCode == 200) {
                    val entity1 = response1.entity

                    val data = mapper.readValue<List<Map<String, Object>>>(entity1.content)
                    for (project in data) {
                        val key = project.get("key") as String
                        val msr = project.get("msr") as List<Map<String, String>>?
                        val ncloc = getMetric(msr, "ncloc") as Double?
                        val coverage = getMetric(msr, "coverage") as Double?
                        logger.info("-> Data $key $ncloc $coverage")
                        map.put(key, SonarIndicators(key, ncloc, coverage))
                    }
                    EntityUtils.consume(entity1)
                }
            } finally {
                response1.close()
            }
        } catch (e: Exception) {
            logger.error("", e)
        }

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