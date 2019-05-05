package org.neo.gomina.plugins

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Inject
import com.google.inject.name.Named
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.integration.ssh.SshAnalysis
import org.neo.gomina.integration.ssh.sudo
import org.neo.gomina.integration.zmqmonitoring.MonitoringMapper
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import org.neo.gomina.model.host.HostSshDetails
import org.neo.gomina.model.host.HostUtils
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.monitoring.*
import org.neo.gomina.model.version.Version
import java.io.File
import java.time.Clock
import java.time.LocalDateTime

class CustomSshAnalysis : SshAnalysis {

    override fun instances(session: Session, sudo: String?, instances: List<Instance>): Map<String, InstanceSshDetails> {
        val cmds = instances.flatMap {
            listOf(
                    """
                        echo "${it.id}.version=`cat ${it.folder}/current/version.txt 2>/dev/null`"
                    """.trimIndent(),
                    //"echo \"${it.id}.version=`cat ${it.folder}/current/version.properties 2>/dev/null | grep version`\"",
                    "echo \"${it.id}.revision=`cat ${it.folder}/current/version.properties 2>/dev/null | grep revision | cut -c14-`\"",
                    //"echo \"${it.id}.version2=`ls -ll $it.folder/current/libs`\"",
                    "echo \"${it.id}.conf.revision=`svn info ${it.folder}/config 2>/dev/null | grep Revision: |cut -c11-`\"",
                    """
                        echo "${it.id}.conf.status=$(echo "$(
                            result=$(svn status ${it.folder}/config 2>&1);
                            notvers=$(echo ${"$"}result | grep 'is not a working copy');
                            if [ ! -z "${"$"}notvers" ]; then
                                echo 'not versioned';
                            elif [ ! -z "${"$"}result" ]; then
                                echo 'modified'; else echo 'ok';
                            fi
                        )")"
                    """.trimIndent()
                    //"echo \"${it.id}.conf.status=$(svn status ${it.folder}/config)\""
            )
        }
        val result = session.sudo(sudo, cmds)
                .lines()
                .filter { it.contains('=') }
                .map { it.split('=', limit = 2) }
                .map { it[0] to it[1] }
                .toMap()

        result.forEach { k, v -> println("  $k = $v") }

        return instances.map { instance ->
            instance.folder!! to InstanceSshDetails(
                    analyzed = true,
                    deployedVersion = result["${instance.id}.version"], // deployedVersion(session, sudo, instance.folder),
                    deployedRevision = result["${instance.id}.revision"],
                    confRevision = result["${instance.id}.conf.revision"], //confRevision(session, sudo, instance.folder),
                    confCommitted = when (result["${instance.id}.conf.status"]) {
                        "ok" -> true
                        "modified" -> false
                        "not versioned" -> null
                        else -> null
                    }, //checkConfCommited(session, sudo, instance.folder),
                    confUpToDate = null
            )
        }
        .toMap()
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
    @Inject lateinit var hostUtils: HostUtils
    override fun map(instanceId: String, indicators: Map<String, String>): RuntimeInfo? {
        return if (indicators["STATUS"] != null && indicators["VERSION"] != null) {
            val type = indicators["TYPE"]
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
                val redisRole = indicators["REDIS_ROLE"]
                val readOnly: String?
                val aof: String?
                if (status == ServerStatus.LIVE) {
                    readOnly = when {
                        redisRole == "master" -> "rw"
                        "yes".equals(indicators["REDIS_READONLY"].asString, ignoreCase = true) -> "ro"
                        else -> "rw"
                    }
                    aof = if ("1" == indicators["REDIS_AOF"].asString) "AOF" else "RDB"
                }
                else {
                    readOnly = ""
                    aof = ""
                }
                listOf(
                        "redis.host" to indicators["REDIS_HOST"],
                        "redis.port" to indicators["REDIS_PORT"].asInt,
                        "redis.master" to indicators["REDIS_MASTER"].asBoolean,
                        "redis.status" to indicators["REDIS_STATE"],
                        "redis.role" to redisRole,
                        "redis.rw" to readOnly,
                        "redis.persistence.mode" to aof,
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
                    service = indicators["SERVICE"],
                    lastTime = LocalDateTime.now(Clock.systemUTC()),
                    delayed = false,
                    process = ProcessInfo(
                            pid = indicators["PID"],
                            host = hostUtils.resolve(host),
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

class InteractionsFileProvider : InteractionsProvider {

    companion object {
        private val logger = LogManager.getLogger(InteractionsFileProvider.javaClass)
    }

    @Inject @Named("interactions.file") private lateinit var file: File
    @Inject private lateinit var providers: InteractionProviders

    val jsonMapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject fun init() {
        providers.providers.add(this)
    }

    fun read(file: File): List<Interactions> {
        return when (file.extension) {
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .json")
        }
    }

    override fun name(): String {
        return "auto2"
    }

    override fun getAll(): List<Interactions> {
        return read(file)
    }
}

class CustomInteractionProvider : InteractionsProvider {
    @Inject lateinit var providers: InteractionProviders
    @Inject lateinit var componentsRepo: ComponentRepo
    @Inject lateinit var enrichDependencies: EnrichDependencies
    @Inject fun init() {
        providers.providers.add(this)
    }

    override fun name(): String {
        return "auto"
    }

    override fun getAll(): List<Interactions> {
        // Get the dependencies
        val misReadOnly = listOf("BASKET", "ORDER", "EXEC", "ORDER_MARKET", "EXEC_MARKET", "BOOKING")
        println("-- Dependencies -----------------")

        val interactions = componentsRepo.getAll()
                .mapNotNull { c -> c.artifactId?.let { c.id to ArtifactId.tryWithGroup(it) } }
                .flatMap { (cid, artifactId) -> artifactId?.let { listOf(cid to artifactId) } ?: emptyList() }
                .mapNotNull { (cId, artifactId) ->
                    when (cId) {
                        "tradex-mis" -> XDepSource.get(cId, artifactId.groupId ?: "", artifactId.artifactId, "1.3.2-SNAPSHOT")
                                ?.apply {
                                    this.api?.raised = emptyMap()
                                    this.dependencies?.redis?.forEach {
                                        if (misReadOnly.contains(it.topic)) it.type = "R"
                                    }
                                }
                        else -> XDepSource.get(cId, artifactId.groupId ?: "", artifactId.artifactId)
                    }
        }
        /*
        println("---------------------------------")
        val result1 = listOf(
                XDepSource.get("tradex-referential", "x.oms.referential", "tradex-referential"),
                XDepSource.get("tradex-position", "x.oms.position", "tradex-position"),
                XDepSource.get("tradex-marketdata", "x.oms.marketdata", "tradex-marketdata"),
                XDepSource.get("tradex-basketmanager", "x.oms.vac", "voms-basketmanager"),
                XDepSource.get("tradex-ordermanager", "x.oms.vac", "voms-ordermanager"),
                XDepSource.get("tradex-marketmanager", "x.oms.vac", "voms-marketmanager"),
                XDepSource.get("tradex-crossbroker", "x.oms.vac", "voms-crossbroker"),
                XDepSource.get("tradex-execution", "x.oms.execution", "tradex-execution"),
                XDepSource.get("tradex-clientrfq", "x.oms.clientrfq", "tradex-clientrfq"),
                XDepSource.get("tradex-fixout", "x.oms.fixout", "tradex-fixout"),
                XDepSource.get("tradex-emmabroker", "x.oms.emmabroker", "tradex-emmabroker"),
                XDepSource.get("tradex-liquidityprovider", "x.oms.liquidityprovider", "tradex-liquidityprovider"),
                XDepSource.get("tradex-fidessa", "x.oms.fidessa", "tradex-fidessa", "3.0.1-SNAPSHOT"),
                XDepSource.get("tradex-posttrade-app", "x.oms.posttrade", "tradex-posttrade-app"),
                XDepSource.get("tradex-pretrade-app", "x.oms.pretrade", "tradex-pretrade-app"),
                XDepSource.get("tradex-fixinest", "x.oms.fixinest", "tradex-fixinest"),
                XDepSource.get("tradex-fixin", "x.oms.fixin", "tradex-fixin", "1.4.1-SNAPSHOT"),
                XDepSource.get("tradex-booking", "x.oms.booking", "tradex-booking"),
                XDepSource.get("tradex-ioi", "x.oms.ioi", "tradex-ioi"),
                XDepSource.get("tradex-interest", "x.oms.interest", "tradex-interest"),
                XDepSource.get("tradex-advert", "x.oms.advert", "tradex-advert"),
                //XDepSource.get("tradex-position", "x.oms.position", "tradex-position"),
                XDepSource.get("tradex-brokerrfq", "x.oms.brokerrfq", "tradex-brokerrfq"),
                XDepSource.get("tradex-bookbroker", "x.oms.bookbroker", "tradex-bookbroker"),
                XDepSource.get("tradex-facilitation", "x.oms.facilitation", "tradex-facilitation"),
                XDepSource.get("tradex-fakebroker", "x.oms.fakebroker", "tradex-fakebroker"),
                XDepSource.get("tradex-pnl", "x.oms.pnl", "tradex-pnl"),
                XDepSource.get("tradex-mis", "x.oms.mis", "tradex-mis", "1.3.2-SNAPSHOT")?.apply {
                    this.api?.raised = emptyMap()
                    this.dependencies?.redis?.forEach {
                        if (misReadOnly.contains(it.topic)) it.type = "R"
                    }
                }
                //DataAccess.get("x.oms.routingsync", "tradex-routingsync"),
                //DataAccess.get("x.oms.cli", "tradex-cli")
        )
        .filterNotNull()
        println("---------------------------------")
        */
        // Map to the right format
        val result = interactions.map {d ->
            val apiCmd = d.api?.commands?.flatMap { it.value }?.map { Function(it, "cmd") } ?: emptyList()
            val apiCmI = d.api?.commands?.flatMap { it.value }?.map { Function(it, "cmd-internal") } ?: emptyList()
            val apiEvt = d.api?.raised?.flatMap { it.value }?.map { Function(it, "evt") } ?: emptyList()
            val depCmd = d.dependencies?.commands?.flatMap { it.value }?.map { FunctionUsage(it, "cmd", Usage("CMD-INVOCATION")) } ?: emptyList()
            val depCmI = d.dependencies?.commandsInternal?.flatMap { it.value }?.map { FunctionUsage(it, "cmd-internal", Usage("CMD-INVOCATION")) } ?: emptyList()
            val depEvt = d.dependencies?.events?.flatMap { it.value }?.map { FunctionUsage(it, "evt", Usage("EVT-INVOCATION")) } ?: emptyList()
            val depRed = d.dependencies?.redis?.map { FunctionUsage(it.redisName ?: "", "database", Usage(it.type ?: "")) } ?: emptyList()
            Interactions(d.component, apiCmd + apiCmI + apiEvt, depCmd + depCmI + depEvt + depRed)
        }

        val specialInteractions = interactions
            .map { d ->
                val entDep = d.dependencies?.redis?.map { FunctionUsage("${it.redisName}.${it.topic}", "topic", Usage(it.type ?: "")) } ?: emptyList()
                Interactions(d.component, emptyList(), entDep)
            }
            .let { Dependencies.functions(it) }
            .flatMap { (f, stakeholders) ->
                listOf(
                    Pair(Function(f.name, "db-read", listOf("inferred")), Dependencies.infer("inferred", stakeholders.users, "R", "RW") { it?.usage }),
                    Pair(Function(f.name, "db-multi", listOf("inferred")), Dependencies.infer("inferred", stakeholders.users, "RW", "RW") { it?.usage })
                )
            }
            .toMap()
            .let { Dependencies.interactions(it) }

        return (result + specialInteractions).merge().toList()
        //return listOf(fixin, order, orderExt, basket, referential)
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
                    val function = Function(f.name, "db-interaction", listOf("inferred"))
                    val stakeHolders = Dependencies.infer("inferred", stakeholders.users, "R", "RW") { it?.usage }
                    Pair(function, stakeHolders)
                }
                .toMap()
        return Dependencies.interactions(specialFunctions)
    }

}