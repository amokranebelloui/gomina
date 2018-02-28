package org.neo.gomina.api.realtime

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import org.apache.logging.log4j.LogManager
import java.util.*
import javax.inject.Inject

class NotificationsApi {

    companion object {
        private val logger = LogManager.getLogger(NotificationsApi::class.java)
    }

    val router: Router
    val sockets = ArrayList<SockJSSocket>()

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
}