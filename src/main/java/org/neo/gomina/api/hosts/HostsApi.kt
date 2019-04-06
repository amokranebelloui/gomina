package org.neo.gomina.api.hosts

import com.fasterxml.jackson.databind.ObjectMapper
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
        val tags: List<String> = emptyList(),

        val username: String? = null,
        val passwordAlias: String? = null,
        val sudo: String? = null,

        val unexpected: List<String> = emptyList()
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

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::hosts)
        router.get("/:hostId").handler(this::host)
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
                    .filter { !it.isBlank() }
                    .filter { !hostMap.containsKey(it) }
                    .map { HostDetail(host = it, managed = false) }

            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hostMap.values + unmanaged))
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
            tags = tags,
            username = username,
            passwordAlias = passwordAlias,
            sudo = sudo,
            unexpected = unexpectedFolders
    )
}
