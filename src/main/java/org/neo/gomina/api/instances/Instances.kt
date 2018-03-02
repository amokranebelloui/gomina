package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.instances.Instance
import org.neo.gomina.model.instances.Instances
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.scm.ScmConnector
import org.neo.gomina.plugins.scm.impl.CachedScmConnector
import org.neo.gomina.model.sshinfo.SshConnector
import org.neo.gomina.plugins.monitoring.applyMonitoring
import org.neo.gomina.plugins.scm.applyScm
import org.neo.gomina.plugins.ssh.applyInventory
import org.neo.gomina.plugins.ssh.applySsh
import javax.inject.Inject


class InstancesBuilder {

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var sshConnector: SshConnector
    @Inject private lateinit var monitoring: Monitoring

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var scmConnector: ScmConnector

    fun getInstances(): List<Instance> {
        val instances = Instances()
        sshConnector.analyze()
        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    var instance = instances.ensure(id, env.id, service.type, service.svc, envInstance.id)
                    instance.applyInventory(service, envInstance)

                    val project = if (service.project != null) projects.getProject(service.project) else null
                    project?.let { instance.applyScm(scmConnector.getSvnDetails(project.svnRepo, project.svnUrl)) }

                    instance.applySsh(sshConnector.getDetails(envInstance.host, envInstance.folder))
                }
            }
        }

        for (env in inventory.getEnvironments()) {
            val monitoring = this.monitoring.getFor(env.id)
            for ((instanceId, indicators) in monitoring.instances) {
                val id = env.id + "-" + instanceId
                var instance = instances.ensure(id, env.id, indicators["type"], indicators["service"], instanceId, expected = false)
                instance.applyMonitoring(indicators)
            }
        }
        return instances.list
    }

}

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val router: Router

    @Inject private lateinit var instancesBuilder: InstancesBuilder
    @Inject private lateinit var cachedScmConnector: CachedScmConnector
    @Inject private lateinit var projects: Projects
    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.post("/reload").handler(this::reload)
    }

    fun instances(ctx: RoutingContext) {
        try {
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instancesBuilder.getInstances()))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }

    fun reload(ctx: RoutingContext) {
        try {
            logger.info("Reloading ...")
            // FIXME Reload

            for (project in projects.getProjects()) {
                if (StringUtils.isNotBlank(project.svnUrl)) {
                    cachedScmConnector.refresh(project.svnRepo, project.svnUrl)
                }
            }

            ctx.response().putHeader("content-type", "text/javascript").end()
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }
}