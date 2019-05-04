package org.neo.gomina.api.knowledge

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.UserRef
import org.neo.gomina.api.common.toRef
import org.neo.gomina.api.component.ComponentRef
import org.neo.gomina.api.component.toComponentRef
import org.neo.gomina.model.component.ComponentKnowledge
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Knowledge
import org.neo.gomina.model.user.Users
import javax.inject.Inject

data class KnowledgeDetail(val component: ComponentRef, val user: UserRef, val knowledge: Int)

class KnowledgeApi {

    companion object {
        private val logger = LogManager.getLogger(KnowledgeApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var componentKnowledge: ComponentKnowledge
    @Inject lateinit var components: ComponentRepo
    @Inject lateinit var users: Users

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/component/:componentId").handler(this::component)
        router.get("/user/:userId").handler(this::user)
        router.put("/:componentId/user/:userId").handler(this::knowledge)
    }

    private fun component(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            logger.info("Get component '$componentId' knowledge")
            val componentRef = components.get(componentId)?.toComponentRef()
            val userMap = users.getUsers().associateBy { it.id }
            val knowledge = componentKnowledge.componentKnowledge(componentId).mapNotNull { (userId, knowledge) ->
                val userRef = userMap[userId]?.toRef()
                if (componentRef != null && userRef != null) KnowledgeDetail(componentRef, userRef, knowledge.knowledge) else null
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(knowledge))
        }
        catch (e: Exception) {
            logger.error("Cannot get component knowledge", e)
            ctx.fail(500)
        }
    }

    private fun user(ctx: RoutingContext) {
        try {
            val userId = ctx.request().getParam("userId")
            logger.info("Get user '$userId' knowledge")
            val userRef = users.getUser(userId)?.toRef()
            val componentsMap = components.getAll().associateBy { it.id }
            val knowledge = componentKnowledge.userKnowledge(userId).mapNotNull { (componentId, knowledge) ->
                val componentRef = componentsMap[componentId]?.toComponentRef()
                if (componentRef != null && userRef != null) KnowledgeDetail(componentRef, userRef, knowledge.knowledge) else null
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(knowledge))
        }
        catch (e: Exception) {
            logger.error("Cannot get user knowledge", e)
            ctx.fail(500)
        }
    }

    private fun knowledge(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        val userId = ctx.request().getParam("userId")
        val knowledge = ctx.request().getParam("knowledge")?.let { Knowledge(it.toInt()) }
        try {
            logger.info("Update knowledge $componentId $userId $knowledge")
            componentKnowledge.updateKnowledge(componentId, userId, knowledge)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(knowledge))
        }
        catch (e: Exception) {
            logger.error("Cannot update Host", e)
            ctx.fail(500)
        }
    }

}
