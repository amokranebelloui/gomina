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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.api.diagram.DiagramApi;
import org.neo.gomina.api.envs.EnvsApi;
import org.neo.gomina.api.instances.InstancesApi;
import org.neo.gomina.api.projects.ProjectsApi;
import org.neo.gomina.api.realtime.NotificationsApi;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.module.GominaModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebVerticle extends AbstractVerticle {

    private static final Logger logger = LogManager.getLogger(WebVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting...");

        Injector injector = Guice.createInjector(Modules.combine(new GominaModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toInstance(vertx);
            }
        }));

        EnvsApi envsApi = injector.getInstance(EnvsApi.class);
        ProjectsApi projectsApi = injector.getInstance(ProjectsApi.class);
        InstancesApi instancesApi = injector.getInstance(InstancesApi.class);
        DiagramApi diagramApi = injector.getInstance(DiagramApi.class);
        NotificationsApi notificationsApi = injector.getInstance(NotificationsApi.class);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        Monitoring monitoring = injector.getInstance(Monitoring.class);
        final ObjectMapper mapper = new ObjectMapper();
        monitoring.add((env, instanceId, newValues) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("env", env);
            map.put("id", instanceId);
            map.putAll(newValues);
            try {
                String message = mapper.writeValueAsString(map);
                Buffer buffer = Buffer.buffer(message);
                notificationsApi.getSockets().forEach(socket -> socket.write(buffer));
            }
            catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        });

        router
                .mountSubRouter("/data/envs", envsApi.getRouter())
                .mountSubRouter("/data/projects", projectsApi.getRouter())
                .mountSubRouter("/data/instances", instancesApi.getRouter())
                .mountSubRouter("/data/diagram", diagramApi.getRouter())
                .mountSubRouter("/realtime", notificationsApi.getRouter());

        router.get("/*").pathRegex(".*\\.(js|ico|map)").handler(StaticHandler.create("dist").setCachingEnabled(false));

        router.route().pathRegex("/.*").handler(ctx -> {
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

        router.get().failureHandler(ctx -> {
            logger.info("Handling failure {}", ctx.statusCode());
            ctx.response().setStatusCode(ctx.statusCode()).end("Ooops! something went wrong");
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    @Override
    public void stop() {
        logger.info("Stopping...");
    }
}
