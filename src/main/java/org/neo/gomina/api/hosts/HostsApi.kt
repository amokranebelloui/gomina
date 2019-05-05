package org.neo.gomina.api.hosts

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

data class HostDetail(
        val host: String,
        val managed: Boolean,
        val dataCenter: String? = null,
        val group: String? = null,
        val type: String? = null,
        val osFamily: String? = null,
        val os: String? = null,
        val tags: List<String> = emptyList(),

        val username: String? = null,
        val passwordAlias: String? = null,
        val proxyUser: String? = null,
        val proxyHost: String? = null,
        val sudo: String? = null,

        val unexpected: List<String> = emptyList()
)

data class HostData(
        val dataCenter: String?,
        val group: String?, // several servers forming a group
        val type: String?, // PROD, TEST, etc
        val osFamily: String?,
        val os: String?,
        val tags: List<String> = emptyList()
)

data class HostConnectivityData(
        val username: String?,
        val passwordAlias: String? = null,
        val proxyUser: String? = null,
        val proxyHost: String? = null,
        val sudo: String? = null
)

class HostsApi {

    companion object {
        private val logger = LogManager.getLogger(HostsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var hosts: Hosts
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var sshService: SshService

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::hosts)
        router.get("/:hostId").handler(this::host)
        router.post("/add").handler(this::add)
        router.put("/:hostId/update").handler(this::update)
        router.put("/:hostId/update/connectivity").handler(this::updateConnectivity)
        router.post("/:hostId/reload").handler(this::reload)
    }

    private fun hosts(ctx: RoutingContext) {
        try {
            val host = ctx.request().getParam("host")
            logger.info("Host '$host' details")
            val hostMap = hosts.getHosts().map { it.map() }.associateBy { it.host }

            val unmanaged = inventory.getEnvironments()
                    .flatMap { it.services }
                    .flatMap { it.instances }
                    .mapNotNull { it.host } // FIXME Add Running host, after refactoring monitoring data to be on the instance object
                    .toSet()
                    .filter { !it.isBlank() }
                    .filter { !hostMap.containsKey(it) }
                    .map { HostDetail(host = it, managed = false) }

            val hosts = (hostMap.values + unmanaged).sortedBy { it.host }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hosts))
        }
        catch (e: Exception) {
            logger.error("Cannot get hosts", e)
            ctx.fail(500)
        }
    }

    private fun host(ctx: RoutingContext) {
        try {
            val hostId = ctx.request().getParam("hostId")
            logger.info("Host '$hostId' details")
            val host = hosts.getHost(hostId)?.let { it.map() }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(host))
        }
        catch (e: Exception) {
            logger.error("Cannot get host", e)
            ctx.fail(500)
        }
    }

    private fun add(ctx: RoutingContext) {
        val hostId = ctx.request().getParam("hostId")
        try {
            //val data = mapper.readValue<HostData>(ctx.body.toString())
            logger.info("Adding host $hostId")
            hosts.addHost(hostId)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(hostId))
        }
        catch (e: Exception) {
            logger.error("Cannot add Host", e)
            ctx.fail(500)
        }
    }

    private fun update(ctx: RoutingContext) {
        val hostId = ctx.request().getParam("hostId")
        try {
            val data = mapper.readValue<HostData>(ctx.body.toString())
            logger.info("Update host $hostId $data")
            hosts.updateHost(hostId, data.dataCenter, data.group, data.type ?: "UNKNOWN", data.osFamily, data.os, data.tags)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(hostId))
        }
        catch (e: Exception) {
            logger.error("Cannot update Host", e)
            ctx.fail(500)
        }
    }

    private fun updateConnectivity(ctx: RoutingContext) {
        val hostId = ctx.request().getParam("hostId")
        try {
            val d = mapper.readValue<HostConnectivityData>(ctx.body.toString())
            logger.info("Update host $hostId $d")
            hosts.updateConnectivity(hostId, d.username, d.passwordAlias, d.proxyHost, d.proxyUser, d.sudo)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(hostId))
        }
        catch (e: Exception) {
            logger.error("Cannot update Host", e)
            ctx.fail(500)
        }
    }

    private fun reload(ctx: RoutingContext) {
        try {
            val hostId = ctx.request().getParam("hostId")
            logger.info("Host '$hostId' details")
            sshService.processHost(hostId)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end("Reload host SSH info done!")
        }
        catch (e: Exception) {
            logger.error("Cannot reload host", e)
            ctx.fail(500)
        }
    }
}

private fun Host.map(): HostDetail {
    return HostDetail(
            host = host,
            managed = true,
            dataCenter = dataCenter,
            group = group,
            type = type,
            osFamily = osFamily,
            os = os,
            tags = tags,
            username = username,
            passwordAlias = passwordAlias,
            proxyHost = proxyHost,
            proxyUser = proxyUser,
            sudo = sudo,
            unexpected = unexpectedFolders
    )
}
