package org.neo.gomina.api.users

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.user.Users
import javax.inject.Inject

data class UserDetail (
        val id: String,
        val login: String,
        val firstName: String?,
        val lastName: String?,
        var disabled: Boolean
)

class UsersApi {

    companion object {
        private val logger = LogManager.getLogger(UsersApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var users: Users

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::users)
        router.get("/:userId").handler(this::user)
    }

    private fun users(ctx: RoutingContext) {
        try {
            logger.info("Users")
            val hosts = users.getUsers().map {
                UserDetail(
                        id = it.id,
                        login = it.login,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        disabled = it.disabled
                )
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hosts))
        }
        catch (e: Exception) {
            logger.error("Cannot get users", e)
            ctx.fail(500)
        }
    }

    private fun user(ctx: RoutingContext) {
        try {
            val userId = ctx.request().getParam("userId")
            logger.info("User '$userId' details")
            val user = users.getUser(userId)?.let {
                UserDetail(
                        id = it.id,
                        login = it.login,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        disabled = it.disabled
                )
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(user))
        }
        catch (e: Exception) {
            logger.error("Cannot get users", e)
            ctx.fail(500)
        }
    }

}