package org.neo.gomina.runner

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.apache.logging.log4j.LogManager
import org.neo.gomina.web.WebVerticle

fun main(args: Array<String>) {
    val logger = LogManager.getLogger("main")

    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    System.setProperty("vertx.disableFileCaching", "true")
    if (args.isNotEmpty()) System.setProperty("gomina.config.file", args[0])

    System.getProperties().forEach { (key, `val`) -> logger.info(key.toString() + "=" + `val`) }

    val vertx = Vertx.vertx()

    vertx.deployVerticle(WebVerticle::class.java.name, DeploymentOptions().setInstances(1)) { res ->
        if (res.succeeded()) {
            logger.info("Deployment id is: " + res.result())
        }
        else {
            logger.info("Deployment failed! ", res.cause())
        }
    }
}