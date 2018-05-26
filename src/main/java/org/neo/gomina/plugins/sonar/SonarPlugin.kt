package org.neo.gomina.plugins.sonar

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.integration.sonar.SonarConnectors
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import org.neo.gomina.utils.Cache
import javax.inject.Inject

private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

class SonarPlugin : Plugin {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var connectors: SonarConnectors

    private val sonarCache = Cache<Map<String, SonarIndicators>>("sonar")

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository

    override fun init() {
        logger.info("Initializing Sonar Data ...")
        projects.getProjects()
                .groupBy { it.sonarServer }
                .forEach { (srv, projects) ->
                    val metrics = sonarCache.getOrLoad(srv) { getMetrics(srv) }
                    projects.apply(metrics)
                }
        logger.info("Sonar Data initialized")
    }

    fun reload() {
        projects.getProjects()
                .groupBy { it.sonarServer }
                .forEach { (srv, projects) ->
                    getMetrics(srv) ?. let { metrics ->
                        sonarCache.cache(srv, metrics)
                        projects.apply(metrics)
                    }
                }
    }

    private fun getMetrics(srv:String) = connectors.getConnector(srv)?.getMetrics()

    private fun List<Project>.apply(metrics: Map<String, SonarIndicators>?) {
        metrics?. let { metrics ->
            forEach {
                projectDetailRepository.getProject(it.id)?.apply(metrics[it.maven])
            }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(SonarPlugin::class.java)
    }
}