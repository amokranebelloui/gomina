package org.neo.gomina;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WebVerticle extends AbstractVerticle {

    private static final Logger logger = LogManager.getLogger(WebVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting...");
        Router router = Router.router(vertx);

        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        List<SockJSSocket> sockets = new ArrayList<>();
        sockJSHandler.socketHandler(sockJSSocket -> {
            logger.info("Handling SockJS " + sockJSSocket);
            logger.info("Started " + sockJSSocket.writeHandlerID());
            sockets.add(sockJSSocket);
            // Just echo the data back
            sockJSSocket.handler(buffer -> {
                logger.info("Event " + buffer.toString());
                sockJSSocket.write(buffer);
            });
            sockJSSocket.endHandler(Void -> {
                //logger.info("Event " + buffer.toString());
                logger.info("Ended " + sockJSSocket.writeHandlerID());
                sockets.remove(sockJSSocket);
            });
        });
        router.route("/realtime/*").handler(sockJSHandler);

        router.route("/*").handler(StaticHandler.create("web").setCachingEnabled(false).setIndexPage("index.html"));

        Thread thread = new Thread(() -> {
            while (true) {
                String message = "Sample " + System.currentTimeMillis();
                Buffer buffer = Buffer.buffer(message);
                sockets.forEach(socket -> socket.write(buffer));
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    logger.info("", e);
                }
            }
        });
        thread.start();

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(8080);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping...");
    }
}
