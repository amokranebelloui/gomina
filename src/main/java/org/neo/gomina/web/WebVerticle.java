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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.api.diagram.DiagramApi;
import org.neo.gomina.api.envs.EnvBuilder;
import org.neo.gomina.api.instances.InstancesBuilder;
import org.neo.gomina.api.projects.ProjectDetail;
import org.neo.gomina.api.projects.ProjectsBuilder;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.scminfo.impl.CachedScmConnector;
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
        ProjectsBuilder projectBuilder = injector.getInstance(ProjectsBuilder.class);
        EnvBuilder envBuilder = injector.getInstance(EnvBuilder.class);
        InstancesBuilder instancesBuilder = injector.getInstance(InstancesBuilder.class);

        CachedScmConnector cachedScmConnector = injector.getInstance(CachedScmConnector.class);
        Projects projects = injector.getInstance(Projects.class);


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

        router.get("/data/envs").handler(ctx -> {
            try {
                ctx.response().putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(envBuilder.getEnvs()));
            }
            catch (Exception e) {
                logger.error("Cannot get projects", e);
                ctx.fail(500);
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
                    logger.info("Cannot get project " + projectId);
                    ctx.fail(404);
                }
            }
            catch (Exception e) {
                logger.error("Cannot get project", e);
                ctx.fail(500);
            }
        });
        router.post("/data/project/:projectId/reload").handler(ctx -> {
            try {
                String projectId = ctx.request().getParam("projectId");
                Project project = projects.getProject(projectId);
                cachedScmConnector.refresh(project.getSvnRepo(), project.getSvnUrl());
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

                for (Project project : projects.getProjects()) {
                    if (StringUtils.isNotBlank(project.getSvnUrl())) {
                        cachedScmConnector.refresh(project.getSvnRepo(), project.getSvnUrl());
                    }
                }

                ctx.response().putHeader("content-type", "text/javascript").end();
            }
            catch (Exception e) {
                logger.error("Cannot get instances", e);
                ctx.fail(500);
            }
        });

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
