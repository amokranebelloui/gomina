package org.neo.gomina.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.api.diagram.DiagramApi;
import org.neo.gomina.api.envs.EnvsApi;
import org.neo.gomina.api.instances.InstancesApi;
import org.neo.gomina.api.projects.ProjectsApi;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.module.GominaModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebVerticle extends AbstractVerticle {

    private static final Logger logger = LogManager.getLogger(WebVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting...");
        Router router = Router.router(vertx);

        Injector injector = Guice.createInjector(Modules.combine(new GominaModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toInstance(vertx);
            }
        }));

        Monitoring monitoring = injector.getInstance(Monitoring.class);

        EnvsApi envsApi = injector.getInstance(EnvsApi.class);
        ProjectsApi projectsApi = injector.getInstance(ProjectsApi.class);
        InstancesApi instancesApi = injector.getInstance(InstancesApi.class);
        DiagramApi diagramApi = injector.getInstance(DiagramApi.class);


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

        router.route().handler(BodyHandler.create());

        final ObjectMapper mapper = new ObjectMapper();

        monitoring.add((env, instanceId, newValues) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("env", env);
            map.put("id", instanceId);
            map.putAll(newValues);
            try {
                String message = mapper.writeValueAsString(map);
                Buffer buffer = Buffer.buffer(message);
                sockets.forEach(socket -> socket.write(buffer));
            }
            catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        router.mountSubRouter("/data/envs", envsApi.getRouter());
        router.mountSubRouter("/data/projects", projectsApi.getRouter());
        router.mountSubRouter("/data/instances", instancesApi.getRouter());
        router.mountSubRouter("/data/diagram", diagramApi.getRouter());

        /*
        final Handlebars handlebars = new Handlebars(new FileTemplateLoader("web"));
        router.route().pathRegex("/(.*)\\.hbs").handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/html");
            try {
                String templateName = ctx.request().getParam("param0");
                Template template = handlebars.compile(templateName);
                response.end(template.apply("test data"));
            }
            catch (IOException e) {
                ctx.fail(404);
                e.printStackTrace();
            }
        });
        */

        router.get("/*").pathRegex(".*\\.(js|ico|map)").handler(StaticHandler.create("dist").setCachingEnabled(false));

        router.route().pathRegex("/.*").handler(ctx -> {
        //router.route().pathRegex("/([^/.]*)/*.*").handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/html");
            try {
                String root = ctx.request().getParam("param0");
                logger.info("root " + root);
                String filename = "dist/index.html";
                if (!new File(filename).exists()) {
                    throw new Exception("App " + root + " doesn't exist");
                }
                response.sendFile(filename);
            }
            catch (Exception e) {
                logger.error("Error serving static data", e);
                ctx.fail(404);
            }
        });

        //router.route("/*").handler(StaticHandler.create("dist").setCachingEnabled(false));

        /*
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
        */

        router.get().failureHandler(ctx -> {
            logger.info("Handling failure {}", ctx.statusCode());
            ctx.response().setStatusCode(ctx.statusCode()).end("Ooops! something went wrong");

        });

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(8080);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping...");
    }
}
