package org.neo.gomina.api.realtime

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.instances.InstanceListener
import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import java.util.*
import javax.inject.Inject

class NotificationsApi {

    companion object {
        private val logger = LogManager.getLogger(NotificationsApi::class.java)
    }

    val router: Router
    val sockets = ArrayList<SockJSSocket>()

    @Inject lateinit var monitoringPlugin: MonitoringPlugin

    @Inject
    constructor(vertx: Vertx) {
        val options = SockJSHandlerOptions().setHeartbeatInterval(2000)
        val sockJSHandler = SockJSHandler.create(vertx, options)
        sockJSHandler.socketHandler({ sockJSSocket ->
            logger.info("Handling SockJS " + sockJSSocket)
            logger.info("Started " + sockJSSocket.writeHandlerID())
            sockets.add(sockJSSocket)
            // Just echo the data back
            sockJSSocket.handler({ buffer ->
                logger.info("Event " + buffer.toString())
                sockJSSocket.write(buffer)
            })
            sockJSSocket.endHandler({ Void ->
                //logger.info("Event " + buffer.toString());
                logger.info("Ended " + sockJSSocket.writeHandlerID())
                sockets.remove(sockJSSocket)
            })
        })

        this.router = Router.router(vertx)
        router.route("/*").handler(sockJSHandler)
    }

    fun start() {
        val mapper = ObjectMapper()
        val instanceListener: InstanceListener = { instance ->
            try {
                val message = mapper.writeValueAsString(instance)
                logger.info("Real time Update $instance")
                val buffer = Buffer.buffer(message)
                this.sockets.forEach { socket -> socket.write(buffer) }
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
        }
        monitoringPlugin.registerListener(instanceListener)
    }
}