package org.neo.gomina.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.user.Users
import javax.inject.Inject

class AuthApi {

    companion object {
        private val logger = LogManager.getLogger(AuthApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var users: Users

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.post("/").handler(this::authenticate)
    }

    private fun authenticate(ctx: RoutingContext) {
        try {
            val username = ctx.bodyAsJson.getString("username")
            val password = ctx.bodyAsJson.getString("password")
            logger.info("Authenticate '$username' ")

            // FIXME Authentication and permissions
            val userid = username
            val permissions = listOf(
                    "component.knowledge",
                    "component.disable",
                    "component.delete"
            )

            var token = mapOf("userid" to userid, "token" to "T0K3N-$username", "permissions" to permissions)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(token))
        }
        catch (e: Exception) {
            logger.error("Cannot Authenticate", e)
            ctx.fail(500)
        }
    }

}