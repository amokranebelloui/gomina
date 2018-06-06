package org.neo.gomina.plugins.monitoring.zmq

import org.fest.assertions.Assertions.assertThat
import org.fest.assertions.MapAssert.entry
import org.junit.Test
import org.neo.gomina.integration.monitoring.Monitoring
import org.neo.gomina.integration.zmqmonitoring.MessageParser
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThreadPool
import org.zeromq.ZMQ
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MonitorXTest {

    @Test
    fun testParser() {
        val parse = MessageParser.parse(".#HB.UAT.kernel.*.0;status=DOWN;quickfixPersistence=ORACLE")
        println(parse)
        assertThat(parse.env).isEqualTo("UAT")
        assertThat(parse.instanceId).isEqualTo("kernel")
        assertThat(parse.indicators).includes(entry("status", "DOWN"), entry("quickfixPersistence", "ORACLE"))
    }

    @Test
    fun testZmq() {

        val monitoring = Monitoring()
        monitoring.timeoutSeconds = 7
        //val plugin = MonitoringPlugin()
        //plugin.monitoring = monitoring

        val counter = AtomicInteger(0)
        monitoring.onMessage { env, instanceId, indicators ->
            println("received $env $instanceId $indicators")
            assertThat(env).isEqualTo("UAT")
            assertThat(instanceId).isEqualTo("kernel")
            assertThat(indicators["status"]).isNotEmpty
            //assertThat(newValues.containsKey("quickfixPersistence")).isTrue()
            counter.incrementAndGet()
        }

        // Prepare
        fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"
        monitoring.enrich = { indicators ->
            indicators.put("TIMESTAMP", Date().toString())
            indicators["status"]?.let { status -> indicators.put("STATUS", mapStatus(status)) }
        }
        monitoring.include = { it["STATUS"] != null && it["VERSION"] != null }
        monitoring.checkFields(setOf("PARTICIPATING", "LEADER", "STATUS"))
        monitoring.onDelay {
            mapOf("STATUS" to "NOINFO")
        }

        val url = "tcp://localhost:7073"
        val pool = ZmqMonitorThreadPool()
        pool.monitoring = monitoring
        pool.add(url, Arrays.asList(""))

        val context = ZMQ.context(1)
        val subscriber = context.socket(ZMQ.PUB)
        subscriber.bind(url)
        Thread.sleep(1000) // Connection to be established

        subscriber.send(".#HB.UAT.kernel.*.0;status=DOWN;quickfixPersistence=ORACLE;VERSION=12")
        println("Sent 1")
        Thread.sleep(1400)
        subscriber.send(".#HB.UAT.kernel.*.0;status=LIVE;quickfixPersistence=ORACLE;VERSION=12")
        println("Sent 2")
        Thread.sleep(200)

        subscriber.close()
        assertThat(monitoring.instancesFor("UAT")).hasSize(1)
        assertThat(counter.get()).isEqualTo(2)

    }
}
