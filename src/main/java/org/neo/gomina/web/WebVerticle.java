package org.neo.gomina.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.api.instances.InstancesBuilder;
import org.neo.gomina.api.projects.ProjectDetail;
import org.neo.gomina.api.projects.ProjectsBuilder;
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

        Injector injector = Guice.createInjector(new GominaModule());

        Monitoring monitoring = injector.getInstance(Monitoring.class);
        ProjectsBuilder projectBuilder = injector.getInstance(ProjectsBuilder.class);
        InstancesBuilder instancesBuilder = injector.getInstance(InstancesBuilder.class);


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

        router.get("/data/projects").handler(ctx -> {
            try {
                ctx.response().putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(projectBuilder.getProjects()));
            }
            catch (Exception e) {
                logger.error("Cannot get projects", e);
                ctx.fail(500);
            }
        });

        router.get("/data/project/:projectId").handler(ctx -> {
            try {
                String projectId = ctx.request().getParam("projectId");
                ProjectDetail project = projectBuilder.getProject(projectId);
                if (project != null) {
                    ctx.response().putHeader("content-type", "text/javascript")
                            .end(mapper.writeValueAsString(project));
                }
                else {
                    ctx.fail(404);
                }
            }
            catch (Exception e) {
                logger.error("Cannot get project", e);
                ctx.fail(500);
            }
        });

        router.get("/data/instances").handler(ctx -> {
            try {
                ctx.response()
                        .putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(instancesBuilder.getInstances()));
            }
            catch (Exception e) {
                logger.error("Cannot get instances", e);
                ctx.fail(500);
            }
        });

        router.post("/data/instances/reload").handler(ctx -> {
            try {
                logger.info("Reloading ...");
                // FIXME Reload
                ctx.response().putHeader("content-type", "text/javascript").end();
            }
            catch (Exception e) {
                logger.error("Cannot get instances", e);
                ctx.fail(500);
            }
        });

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
