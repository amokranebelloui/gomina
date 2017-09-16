package org.neo.gomina;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebVerticle extends AbstractVerticle {

    private static final Logger logger = LogManager.getLogger(WebVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting...");
        Router router = Router.router(vertx);

        router.route("/*").handler(StaticHandler.create("web").setCachingEnabled(false).setIndexPage("index.html"));

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(8080);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping...");
    }
}
