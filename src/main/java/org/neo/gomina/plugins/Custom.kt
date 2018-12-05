package org.neo.gomina.plugins

import com.google.inject.Inject
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.ssh.SshAnalysis
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.integration.zmqmonitoring.MonitoringMapper
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.host.resolveHostname
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.monitoring.*
import org.neo.gomina.model.version.Version
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
        try {
            var result = session.sudo(user, "cat $applicationFolder/current/version.txt 2>/dev/null")
            result = when {
                result.contains("No such file or directory") -> "?"
                else -> result
            }
            result = StringUtils.trim(result)
            if (StringUtils.isBlank(result)) {
                result = session.sudo(user, "ls -ll $applicationFolder/current")
                result = when {
                    result.contains("No such file or directory") -> "?"
                    else -> result
                }
                val pattern = ".*versions/.*-([0-9\\.]+(-SNAPSHOT)?)/"
                result = result.replace(pattern.toRegex(), "$1").trim { it <= ' ' }
            }
            return result
        }
        catch (e: Exception) {
            return "?"
        }
    }

}

class CustomMonitoringMapper : MonitoringMapper {
    override fun map(instanceId: String, indicators: Map<String, String>): RuntimeInfo? {
        return if (indicators["STATUS"] != null && indicators["VERSION"] != null) {
            val type = indicators["type"]
            var status = mapStatus(indicators["STATUS"])

            var cluster = indicators["ELECTION"].asBoolean ?: false
            var participating = indicators["PARTICIPATING"].asBoolean ?: false
            var leader = indicators["LEADER"].asBoolean ?: true

            var host = indicators["IP"]

            var version = indicators["VERSION"]
            var revision = indicators["REVISION"]

            // Sidecar managed processes
            var sidecarStatus = mapStatus(indicators["STATUS"])
            if (type == "redis") {
                status = if (status == ServerStatus.LIVE) mapStatus(indicators["REDIS_STATE"]) else ServerStatus.DARK
                cluster = true
                participating = true
                leader = indicators["REDIS_MASTER"].asBoolean ?: false
                host = indicators["REDIS_HOST"]
                version = "2.8.9" // FIXME Retrieve correct value
                revision= ""
            }

            val properties = mapOf(
                    "jvm.jmx.port" to indicators["JMX"].asInt,
                    "xxx.bux.version" to indicators["BUS"],
                    "xxx.core.version" to indicators["CORE"],
                    "quickfix.persistence" to indicators["QUICKFIX_MODE"]
            )

            // FIXME Avoid this technology specific switches 
            val redis = if (type == "redis") {
                listOf(
                        "redis.host" to indicators["REDIS_HOST"],
                        "redis.port" to indicators["REDIS_PORT"].asInt,
                        "redis.master" to indicators["REDIS_MASTER"].asBoolean,
                        "redis.status" to indicators["REDIS_STATE"],
                        "redis.role" to indicators["REDIS_ROLE"],
                        "redis.rw" to if ("yes".equals(indicators["REDIS_READONLY"], ignoreCase = true)) "ro" else "rw",
                        "redis.persistence.mode" to if ("1" == indicators["REDIS_AOF"]) "AOF" else "RDB",
                        "redis.offset" to indicators["REDIS_OFFSET"].asLong,
                        "redis.slave.count" to indicators["REDIS_SLAVES"].asInt,
                        "redis.client.count" to indicators["REDIS_CLIENTS"].asInt
                )

            } else emptyList()

            val redisSlave = if (type == "redis" && indicators["REDIS_ROLE"] == "SLAVE") {
                listOf(
                        "redis.master.host" to indicators["REDIS_MASTER_HOST"],
                        "redis.master.port" to indicators["REDIS_MASTER_PORT"].asInt,
                        "redis.master.link" to mapOf(
                                "status" to ("up" == indicators["REDIS_MASTER_LINK"]),
                                "downSince" to indicators["REDIS_MASTER_LINK_DOWN_SINCE"]),
                        "redis.master.offset.diff" to indicators["REDIS_OFFSET_DIFF"].asLong
                )
            } else emptyList()

            RuntimeInfo(
                    instanceId = instanceId,
                    type = type,
                    service = indicators["service"],
                    lastTime = LocalDateTime.now(Clock.systemUTC()),
                    delayed = false,
                    process = ProcessInfo(
                            pid = indicators["PID"],
                            host = resolveHostname(host),
                            status = status,
                            startTime = indicators["START_TIME"].asTime,
                            startDuration = indicators["START_DURATION"].asLong
                    ),
                    sidecarStatus = sidecarStatus,
                    sidecarVersion = indicators["VERSION"],

                    cluster = ClusterInfo(
                            cluster = cluster,
                            participating = participating,
                            leader = leader // Historically we didn't have this field
                    ),
                    version = version?.let { Version(version = version, revision = revision) },
                    properties = properties + redis + redisSlave
            )
        }
        else {
            null
        }
    }
}

fun mapStatus(status: String?) =
        if ("SHUTDOWN" == status) ServerStatus.DOWN else status ?: ServerStatus.DOWN



val fixin = Interactions(serviceId = "fixin",
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("basketDb", "database", Usage("READ"))
        )
)
val order = Interactions(serviceId = "order",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val orderExt = Interactions(serviceId = "orderExt",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val basket = Interactions(serviceId = "basket",
        exposed = listOf(
                Function("checkBasket", "command")
        ),
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("checkBasket", "command"),
                FunctionUsage("basketDb", "database", Usage("WRITE"))
        )
)
val referential = Interactions(serviceId = "tradex-referential",
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
        // FIXME Read using XXRawDeps
        return listOf(fixin, order, orderExt, basket, referential)
    }
}

object XXRawDeps {
    fun list(): List<XRawDeps> {
        // Overrides
        val misReadOnly = listOf("BASKET", "ORDER", "EXEC", "ORDER_MARKET", "EXEC_MARKET", "BOOKING")

        println("-- Dependencies -----------------")
        val result = listOf(
                XDepSource.get("x.oms.referential", "tradex-referential"),
                XDepSource.get("x.oms.position", "tradex-position"),
                XDepSource.get("x.oms.marketdata", "tradex-marketdata"),
                XDepSource.get("x.oms.vac", "voms-basketmanager"),
                XDepSource.get("x.oms.vac", "voms-ordermanager"),
                XDepSource.get("x.oms.vac", "voms-marketmanager"),
                XDepSource.get("x.oms.vac", "voms-crossbroker"),
                XDepSource.get("x.oms.execution", "tradex-execution"),
                XDepSource.get("x.oms.clientrfq", "tradex-clientrfq"),
                XDepSource.get("x.oms.fixout", "tradex-fixout"),
                XDepSource.get("x.oms.emmabroker", "tradex-emmabroker"),
                XDepSource.get("x.oms.fidessa", "tradex-fidessa", "3.0.1-SNAPSHOT"),
                XDepSource.get("x.oms.posttrade", "tradex-posttrade-app"),
                XDepSource.get("x.oms.pretrade", "tradex-pretrade-app"),
                XDepSource.get("x.oms.fixinest", "tradex-fixinest"),
                XDepSource.get("x.oms.fixin", "tradex-fixin", "1.4.1-SNAPSHOT"),
                XDepSource.get("x.oms.booking", "tradex-booking"),
                XDepSource.get("x.oms.ioi", "tradex-ioi"),
                XDepSource.get("x.oms.interest", "tradex-interest"),
                XDepSource.get("x.oms.advert", "tradex-advert"),
                XDepSource.get("x.oms.position", "tradex-position"),
                XDepSource.get("x.oms.brokerrfq", "tradex-brokerrfq"),
                XDepSource.get("x.oms.bookbroker", "tradex-bookbroker"),
                XDepSource.get("x.oms.facilitation", "tradex-facilitation"),
                XDepSource.get("x.oms.fakebroker", "tradex-fakebroker"),
                XDepSource.get("x.oms.pnl", "tradex-pnl"),
                XDepSource.get("x.oms.mis", "tradex-mis", "1.3.2-SNAPSHOT")?.apply {
                    this.api?.raised = emptyMap()
                    this.dependencies?.redis?.forEach {
                        if (misReadOnly.contains(it.topic)) it.type = "R"
                    }
                }
                //DataAccess.get("x.oms.routingsync", "tradex-routingsync"),
                //DataAccess.get("x.oms.cli", "tradex-cli")
        )
        println("---------------------------------")
        return result.filterNotNull()
    }
}

class CustomEnrichDependencies : EnrichDependencies {
    override fun enrich(components: Collection<Interactions>): Collection<Interactions> {
        val specialFunctions = components
                .map { p ->
                    Interactions(serviceId = p.serviceId,
                            exposed = p.exposed,
                            used = p.used.filter { it.function.type == "database" })
                }
                .let { Dependencies.functions(it) }
                .filter { (f, stakeholders) -> stakeholders.usageExists }
                .map { (f, stakeholders) ->
                    Pair(Function(f.name, "db-interaction"), Dependencies.infer(stakeholders.users, "READ", "WRITE") { it?.usage })
                }
                .toMap()
        return Dependencies.interactions(specialFunctions)
    }

}