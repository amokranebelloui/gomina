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
            val login = ctx.bodyAsJson.getString("login")
            val password = ctx.bodyAsJson.getString("password")
            logger.info("Authenticate '$login'")

            val authenticated = users.authenticate(login, password)
            if (authenticated != null) {
                logger.info("Authenticated '$login'")
                // FIXME permissions
                val permissions = listOf(
                        "component.add",
                        "component.edit",
                        "component.knowledge",
                        "component.disable",
                        "component.delete",

                        "env.add",
                        "env.delete",
                        "env.manage",

                        "host.add",
                        "host.delete",
                        "host.manage"
                )

                var result = mapOf(
                        "userId" to authenticated.id,
                        "token" to "T0K3N-${authenticated.login}",
                        "permissions" to permissions
                )
                ctx.response()
                        .putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(result))

            }
            else {
                logger.info("Authentication failed '$login'")
                ctx.response().setStatusCode(401).end()
            }

        }
        catch (e: Exception) {
            logger.error("Cannot Authenticate", e)
            ctx.fail(500)
        }
    }

}