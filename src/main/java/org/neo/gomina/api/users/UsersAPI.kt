package org.neo.gomina.api.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.UserRef
import org.neo.gomina.model.user.User
import org.neo.gomina.model.user.Users
import javax.inject.Inject

data class UserDetail (
        val id: String,
        val login: String,
        val shortName: String,
        val firstName: String?,
        val lastName: String?,
        var accounts: List<String> = emptyList(),
        var disabled: Boolean
)

data class UserData (
        val login: String = "",
        val shortName: String = "",
        val firstName: String? = null,
        val lastName: String? = null,
        var accounts: List<String> = emptyList()
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
        router.get("/refs").handler(this::usersRefs)
        router.get("/:userId").handler(this::user)
        router.post("/add").handler(this::add)
        router.put("/:userId/update").handler(this::update)
        router.put("/:userId/reset-password").handler(this::resetPassword)
        router.put("/:userId/change-password").handler(this::changePassword)
        router.put("/:userId/enable").handler(this::enable)
        router.put("/:userId/disable").handler(this::disable)
    }

    private fun users(ctx: RoutingContext) {
        try {
            logger.info("Users")
            val hosts = users.getUsers().map { it.toUserDetail() }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hosts))
        }
        catch (e: Exception) {
            logger.error("Cannot get users", e)
            ctx.fail(500)
        }
    }

    private fun usersRefs(ctx: RoutingContext) {
        try {
            logger.info("Users")
            val hosts = users.getUsers().map { UserRef(it.id, it.shortName) }
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
        val userId = ctx.request().getParam("userId")
        try {
            logger.info("User '$userId' details")
            val user = users.getUser(userId)?.let { it.toUserDetail() }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(user))
        }
        catch (e: Exception) {
            logger.error("Cannot get user $userId", e)
            ctx.fail(500)
        }
    }

    private fun add(ctx: RoutingContext) {
        //val userId = ctx.request().getParam("userId")
        try {
            val data = mapper.readValue<UserData>(ctx.body.toString())
            logger.info("Add User $data")
            val userId = users.addUser(data.login, data.shortName, data.firstName, data.lastName, data.accounts)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot add user", e)
            ctx.fail(500)
        }
    }

    private fun update(ctx: RoutingContext) {
        val userId = ctx.request().getParam("userId")
        try {
            val data = mapper.readValue<UserData>(ctx.body.toString())
            logger.info("Update User '$userId' $data")
            users.updateUser(userId, data.shortName, data.firstName, data.lastName)
            users.changeAccounts(userId, data.accounts)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot Update user $userId", e)
            ctx.fail(500)
        }
    }

    private fun resetPassword(ctx: RoutingContext) {
        val userId = ctx.request().getParam("userId")
        try {
            logger.info("Reset Password '$userId'")
            users.resetPassword(userId)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot Reset Password for $userId", e)
            ctx.fail(500)
        }
    }

    private fun changePassword(ctx: RoutingContext) {
        val userId = ctx.request().getParam("userId")
        try {
            val password = ctx.request().getParam("password")
            logger.info("Change Password '$userId' ***")
            users.changePassword(userId, password)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot Change Password for $userId", e)
            ctx.fail(500)
        }
    }

    private fun enable(ctx: RoutingContext) {
        val userId = ctx.request().getParam("userId")
        try {
            logger.info("Enable user '$userId'")
            users.enable(userId)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot Enable User $userId", e)
            ctx.fail(500)
        }
    }

    private fun disable(ctx: RoutingContext) {
        val userId = ctx.request().getParam("userId")
        try {
            logger.info("Disable user '$userId'")
            users.disable(userId)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(userId))
        }
        catch (e: Exception) {
            logger.error("Cannot Disable User $userId", e)
            ctx.fail(500)
        }
    }

}

fun User.toUserDetail(): UserDetail {
    return UserDetail(
            id = this.id,
            login = this.login,
            shortName = this.shortName,
            firstName = this.firstName,
            lastName = this.lastName,
            accounts = this.accounts,
            disabled = this.disabled
    )
}