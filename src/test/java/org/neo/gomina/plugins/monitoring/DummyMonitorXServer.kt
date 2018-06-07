package org.neo.gomina.plugins.monitoring

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.zeromq.ZMQ
import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern

fun main(args: Array<String>) {
    val repository = DummyMonitorData()

    repository.listEnvs().forEach { println(it) }
    repository.getFor("UAT").values.forEach { println(it) }

    DummyMonitorThread().start()
}

class DummyMonitorData {

    private val mapper = ObjectMapper(YAMLFactory())

    private val inventoryDir = File("datadummy")
    private val pattern = Pattern.compile("mon\\.(.*?)\\.yaml")

    fun listEnvs(): List<String> {
        val files = if (inventoryDir.isDirectory) inventoryDir.listFiles() else emptyArray()
        return files
                .map { file -> Pair(file, pattern.matcher(file.name)) }
                .filter { (_, matcher) -> matcher.find() }
                .mapNotNull { (_, matcher) -> matcher.group(1) }
    }

    fun getFor(env: String): Map<String, MutableMap<String, Any>> {
        return try {
            val monitoring = mutableMapOf<String, MutableMap<String, Any>>()
            val file = File("datadummy/mon.$env.yaml")
            val data = if (file.exists()) mapper.readValue<List<MutableMap<String, Any>>>(file) else emptyList()
            data.forEach { indicators ->
                indicators.put("timestamp", LocalDateTime.now(Clock.systemUTC()))
                monitoring.put(indicators["name"] as String, indicators)
            }
            monitoring
        }
        catch (e: Exception) {
            logger.error("", e)
            emptyMap()
        }
    }

    companion object {
        private val logger = LogManager.getLogger(DummyMonitorData::class.java)
    }

}

class DummyMonitorThread : Thread {
    private val data = DummyMonitorData()
    private val random = Random()
    private val subscriber: ZMQ.Socket

    constructor() {
        val url = "tcp://localhost:7070"
        val context = ZMQ.context(1)
        subscriber = context.socket(ZMQ.PUB)
        subscriber.bind(url)
        Thread.sleep(400) // Connection to be established
    }


    override fun run() {

        data.listEnvs().forEach { env ->
            for ((instanceId, indicators) in data.getFor(env)) {
                send(env, instanceId, indicators)
            }
        }
        while (true) {
            println("Round ..")
            var count = 1
            data.listEnvs().forEach { env ->
                for ((instanceId, indicators) in data.getFor(env)) {
                    val i = random.nextInt(15)
                    val status = when {
                        i in 0..11 -> "LIVE"
                        i == 12 -> "LOADING"
                        i == 13 -> "DOWN"
                        else -> null
                    }
                    if (status != null) {
                        //indicators.put("timestamp", LocalDateTime(DateTimeZone.UTC)) // FIXME Too long
                        indicators.put("STATUS", status)
                        indicators.put("PARTICIPATING", random.nextInt(20) < 19)
                        indicators.put("LEADER", random.nextInt(20) < 2)
                        send(env, instanceId, indicators)
                        count++
                    }
                }
            }
            println("Sent $count messages")
            try {
                Thread.sleep(4000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun send(env: String, instanceId: String, indicators: MutableMap<String, Any>) {
        //println("Send mon $env $instanceId $indicators")
        val body = indicators
                .filter { (key, value) -> value != null }
                .map { (key, value) -> "$key=$value" }
                .joinToString(separator = ";")
        subscriber.send(".#HB.$env.$instanceId.*.0;$body")
    }

}
