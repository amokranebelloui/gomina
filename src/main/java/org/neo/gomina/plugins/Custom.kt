package org.neo.gomina.plugins

import com.google.inject.Inject
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.monitoring.asBoolean
import org.neo.gomina.integration.monitoring.asInt
import org.neo.gomina.integration.monitoring.asLong
import org.neo.gomina.integration.monitoring.asTime
import org.neo.gomina.integration.ssh.HostSshDetails
import org.neo.gomina.integration.ssh.InstanceSshDetails
import org.neo.gomina.integration.ssh.SshAnalysis
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.integration.zmqmonitoring.MonitoringMapper
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import org.neo.gomina.model.host.resolveHostname
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.monitoring.*
import java.time.Clock
import java.time.LocalDateTime

class CustomSshAnalysis : SshAnalysis {

    override fun instance(instance: Instance, session: Session, sudo: String?): InstanceSshDetails {
        return InstanceSshDetails(
                analyzed = true,
                deployedVersion = deployedVersion(session, sudo, instance.folder),
                deployedRevision = null,
                confRevision = confRevision(session, sudo, instance.folder),
                confCommitted = checkConfCommited(session, sudo, instance.folder),
                confUpToDate = null
        )
    }

    override fun host(session: Session, sudo: String?): HostSshDetails {
        val result = session.sudo(sudo, "find /Users/Test/Work -mindepth 1 -maxdepth 1 -type d")
        return when {
            result.contains("No such file or directory") -> HostSshDetails(analyzed = true)
            else -> {
                val unexpected = result.split("\n").filter { it.isNotBlank() }.map { it.trim() }
                HostSshDetails(analyzed = true, unexpectedFolders = unexpected)
            }
        }
    }

    fun actions(session: Session, user: String?, applicationFolder: String, version: String) {
        val deploy = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/release.sh $version"
        val run = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/run-all.sh"
        val stop = "sudo -u svc-ed-int /srv/ed/apps/$applicationFolder/ops/stop-all.sh"
        val whomia = "whoami"
        //val result = executeCommand(session, cmd)
    }

    fun checkConfCommited(session: Session, user: String?, applicationFolder: String?): Boolean? {
        val result = session.sudo(user, "svn status $applicationFolder/config")
        return if (StringUtils.isBlank(result)) java.lang.Boolean.TRUE else if (result.contains("is not a working copy")) null else java.lang.Boolean.FALSE
    }

    fun confRevision(session: Session, user: String?, applicationFolder: String?): String? {
        val result = session.sudo(user, "svn info $applicationFolder/config | grep Revision: |cut -c11-")
        return when {
            result.contains("does not exist") -> "?"
            result.contains("is not a working copy") -> "!svn"
            else -> result
        }
    }

    fun deployedVersion(session: Session, user: String?, applicationFolder: String?): String {
        var result = session.sudo(user, "cat $applicationFolder/current/version.txt 2>/dev/null")
        result = StringUtils.trim(result)
        if (StringUtils.isBlank(result)) {
            result = session.sudo(user, "ls -ll $applicationFolder/current")
            val pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/"
            result = result.replace(pattern.toRegex(), "$1").trim { it <= ' ' }
        }
        return result
    }

}

class CustomMonitoringMapper : MonitoringMapper {
    override fun map(instanceId: String, indicators: Map<String, String>): RuntimeInfo? {
        return if (indicators["STATUS"] != null && indicators["VERSION"] != null) {
            var status = mapStatus(indicators["STATUS"])
            // Sidecar managed processes
            if (indicators["TYPE"] == "redis") {
                if (status == ServerStatus.LIVE) indicators["REDIS_STATE"] else ServerStatus.DARK
            }
            RuntimeInfo(
                    instanceId = instanceId,
                    type = indicators["TYPE"],
                    service = indicators["SERVICE"],
                    lastTime = LocalDateTime.now(Clock.systemUTC()),
                    delayed = false,
                    process = ProcessInfo(
                            pid = indicators["PID"],
                            host = resolveHostname(indicators["IP"]),
                            status = status,
                            startTime = indicators["START_TIME"].asTime,
                            startDuration = indicators["START_DURATION"].asLong
                    ),
                    jvm = JvmInfo(
                            jmx = indicators["JMX"].asInt
                    ),
                    cluster = ClusterInfo(
                            cluster = indicators["ELECTION"].asBoolean ?: false,
                            participating = indicators["PARTICIPATING"].asBoolean ?: false,
                            leader = indicators["LEADER"].asBoolean ?: true // Historically we didn't have this field
                    ),
                    fix = FixInfo(
                            quickfixPersistence = indicators["QUICKFIX_MODE"]
                    ),
                    redis = RedisInfo(
                            redisHost = indicators["REDIS_HOST"],
                            redisPort = indicators["REDIS_PORT"].asInt,
                            redisMasterHost = indicators["REDIS_MASTER_HOST"],
                            redisMasterPort = indicators["REDIS_MASTER_PORT"].asInt,
                            redisMasterLink = "up" == indicators["REDIS_MASTER_LINK"],
                            redisMasterLinkDownSince = indicators["REDIS_MASTER_LINK_DOWN_SINCE"],
                            redisOffset = indicators["REDIS_OFFSET"].asLong,
                            redisOffsetDiff = indicators["REDIS_OFFSET_DIFF"].asLong,
                            redisMaster = indicators["REDIS_MASTER"].asBoolean,
                            redisRole = indicators["REDIS_ROLE"],
                            redisRW = if ("yes".equals(indicators["REDIS_READONLY"], ignoreCase = true)) "ro" else "rw",
                            redisMode = if ("1" == indicators["REDIS_AOF"]) "AOF" else "RDB",
                            redisStatus = indicators["REDIS_STATE"],
                            redisSlaveCount = indicators["REDIS_SLAVES"].asInt,
                            redisClientCount = indicators["REDIS_CLIENTS"].asInt
                    ),
                    version = VersionInfo(
                            version = indicators["VERSION"],
                            revision = indicators["REVISION"]
                    ),
                    dependencies = DependenciesInfo(
                            busVersion = indicators["BUS"],
                            coreVersion = indicators["CORE"]
                    )
            )
        }
        else {
            null
        }
    }
}

fun mapStatus(status: String?) =
        if ("SHUTDOWN" == status) ServerStatus.DOWN else status ?: ServerStatus.DOWN



val fixin = Interactions(projectId = "fixin",
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("basketDb", "database", Usage("READ"))
        )
)
val order = Interactions(projectId = "order",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val orderExt = Interactions(projectId = "orderExt",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val basket = Interactions(projectId = "basket",
        exposed = listOf(
                Function("checkBasket", "command")
        ),
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("checkBasket", "command"),
                FunctionUsage("basketDb", "database", Usage("WRITE"))
        )
)
val referential = Interactions(projectId = "tradex-referential",
        exposed = listOf(
                Function("getCustomer", "request"),
                Function("createCustomer", "request")
        )
)

class CustomInteractionProvider : InteractionsProvider {
    @Inject lateinit var repository: ProviderBasedInteractionRepository
    @Inject fun init() {
        repository.providers.add(this)
    }
    override fun getAll(): List<Interactions> {
        return listOf(fixin, order, orderExt, basket, referential)
    }
}

class CustomEnrichDependencies : EnrichDependencies {
    override fun enrich(projects: Collection<Interactions>): Collection<Interactions> {
        val specialFunctions = projects
                .map { p ->
                    Interactions(projectId = p.projectId,
                            exposed = p.exposed,
                            used = p.used.filter { it.function.type == "database" })
                }
                .let { Dependencies.functions(it) }
                .filter { (f, stakeholders) -> stakeholders.usageExists }
                .map { (f, stakeholders) ->
                    Pair(Function(f.name, "database-write"), Dependencies.infer(stakeholders.users, "READ", "WRITE") { it?.usage })
                }
                .toMap()
        return Dependencies.interactions(specialFunctions)
    }

}