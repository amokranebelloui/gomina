package org.neo.gomina.api.hosts

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import javax.inject.Inject

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
    }

    private fun hosts(ctx: RoutingContext) {
        try {
            val host = ctx.request().getParam("host")
            logger.info("Host '$host' details")
            val hosts = hosts.getHosts().map {
                val unexpected = sshService.unexpectedFolders(it.host)
                it.map(unexpected)
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hosts))
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun host(ctx: RoutingContext) {
        try {
            val hostId = ctx.request().getParam("hostId")
            logger.info("Host '$hostId' details")
            val unexpected = sshService.unexpectedFolders(hostId)
            val host = hosts.getHost(hostId)?.map(unexpected)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(host))
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

}

private fun Host.map(unexpected: List<String>): HostDetail {
    return HostDetail(
            host = this.host,
            dataCenter = this.dataCenter,
            unexpected = unexpected
    )
}
