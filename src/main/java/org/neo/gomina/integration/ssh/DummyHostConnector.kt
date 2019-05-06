package org.neo.gomina.integration.ssh

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.security.Passwords
import java.io.File
import javax.inject.Inject

data class DummyFolder(val folder: String,
        val version: String? = null, val revision: String? = null,
        var confCommitted: Boolean? = null, var confUpToDate: Boolean? = null, var confRevision: String? = null
)

data class DummyHost(val hostname: String, val folders: List<DummyFolder>)

class DummyHostSession(val folders: List<DummyFolder>)

class DummyHostConnector {

    @Inject internal lateinit var hosts: Hosts
    @Inject internal lateinit var passwords: Passwords
    @Inject internal lateinit var sshClient: SshClient

    private val mapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    private fun getData(hostname: String): List<DummyFolder>? {
        return mapper.readValue<List<DummyHost>>(File("datadummy/dummyssh.json"))
                .firstOrNull { StringUtils.equals(it.hostname, hostname) }
                ?.folders
    }

    fun <T> process(host: Host, function: (session: DummyHostSession) -> T?): T? {
        return function.invoke(DummyHostSession(getData(host.host) ?: emptyList()))
    }

    companion object {
        private val logger = LogManager.getLogger(DummyHostConnector::class.java)
    }

}