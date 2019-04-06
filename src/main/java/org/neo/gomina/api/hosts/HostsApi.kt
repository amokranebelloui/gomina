package org.neo.gomina.api.hosts

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.Hosts
import javax.inject.Inject

data class HostDetail(
        val host: String,
        val dataCenter: String?,
        val group: String?,
        val type: String,
        val tags: List<String> = emptyList(),

        val username: String?,
        val passwordAlias: String? = null,
        val sudo: String? = null,

        val unexpected: List<String>
)

class HostsApi {

    companion object {
        private val logger = LogManager.getLogger(HostsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var hosts: Hosts
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
            val hosts = hosts.getHosts().map {
                it.map(sshService.getDetails(it.host))
            }
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
            val host = hosts.getHost(hostId)?.let {
                it.map(sshService.getDetails(hostId))
            }
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

private fun Host.map(details: HostSshDetails?): HostDetail {
    return HostDetail(
            host = host,
            dataCenter = dataCenter,
            group = group,
            type = type,
            tags = tags,
            username = username,
            passwordAlias = passwordAlias,
            sudo = sudo,
            unexpected = details?.unexpectedFolders ?: emptyList()
    )
}
