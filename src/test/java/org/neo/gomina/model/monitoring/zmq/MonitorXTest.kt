package org.neo.gomina.model.monitoring.zmq

import org.fest.assertions.Assertions.assertThat
import org.fest.assertions.MapAssert.entry
import org.junit.Test
import org.neo.gomina.model.monitoring.Monitoring
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
        val url = "tcp://localhost:7070"
        val thread = ZmqMonitorThread(monitoring, url, Arrays.asList(""))
        thread.start()

        val counter = AtomicInteger(0)
        monitoring.add { env, instanceId, newValues ->
            println("received " + newValues)
            assertThat(env).isEqualTo("UAT")
            assertThat(instanceId).isEqualTo("kernel")
            assertThat(newValues.containsKey("status")).isTrue()
            assertThat(newValues.containsKey("quickfixPersistence")).isTrue()
            counter.incrementAndGet()
        }

        val context = ZMQ.context(1)
        val subscriber = context.socket(ZMQ.PUB)
        subscriber.bind(url)
        Thread.sleep(400) // Connection to be established

        subscriber.send(".#HB.UAT.kernel.*.0;status=DOWN;quickfixPersistence=ORACLE")
        println("Sent 1")
        Thread.sleep(1400)
        subscriber.send(".#HB.UAT.kernel.*.0;status=LIVE;quickfixPersistence=ORACLE")
        println("Sent 2")
        Thread.sleep(200)

        subscriber.close()
        assertThat(counter.get()).isEqualTo(2)

    }
}
