package org.neo.gomina.integration.jenkins.jenkins

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.jenkins.JenkinsConnector

data class BuildStatus (
        val id:String,
        val building:Boolean,
        val result:String?,
        val timestamp:Long,
        var url: String
)

class JenkinsConnectorImpl : JenkinsConnector {

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    override fun getStatus(url: String): BuildStatus? {
        try {
            val response = HttpClients.createDefault().execute(HttpGet("$url/lastBuild/api/json"))
            response.use {
                logger.info("-> Result " + it.statusLine)
                if (it.statusLine.statusCode == 200) {
                    val entity1 = it.entity
                    val data = mapper.readValue<BuildStatus>(entity1.content)
                    EntityUtils.consume(entity1)
                    data.url = url
                    return data
                }
            }
        }
        catch (e: Exception) {
            logger.error("", e)
        }
        return null
    }

    companion object {
        private val logger = LogManager.getLogger(JenkinsConnectorImpl::class.java)
    }

}
