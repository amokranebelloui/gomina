package org.neo.gomina.runner;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.WebVerticle;

public class Runner {

    private static final Logger logger = LogManager.getLogger(Runner.class);

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        System.setProperty("vertx.disableFileCaching", "true");

        System.getProperties().forEach((key, val) -> logger.info(key + "=" + val));

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(WebVerticle.class.getName(), new DeploymentOptions().setInstances(1), res -> {
            if (res.succeeded()) {
                logger.info("Deployment id is: " + res.result());
            }
            else {
                logger.info("Deployment failed! ", res.cause());
            }
        });
    }
}
