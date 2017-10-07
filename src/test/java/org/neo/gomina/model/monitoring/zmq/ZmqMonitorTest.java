package org.neo.gomina.model.monitoring.zmq;

import org.junit.Test;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.file.FileInventory;
import org.neo.gomina.model.monitoring.Monitoring;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fest.assertions.Assertions.assertThat;

public class ZmqMonitorTest {

    @Test
    public void testZmq() throws Exception {

        Inventory inventory = new FileInventory();
        Monitoring monitoring = new Monitoring();
        ZmqMonitorThread thread = new ZmqMonitorThread(monitoring, inventory);
        thread.start();

        AtomicInteger counter = new AtomicInteger(0);
        monitoring.add((env, instanceId, newValues) -> {
            System.out.println("received " + newValues);
            assertThat(env).isEqualTo("UAT");
            assertThat(instanceId).isEqualTo("kernel");
            assertThat(newValues.containsKey("status")).isTrue();
            assertThat(newValues.containsKey("quickfixPersistence")).isTrue();
            counter.incrementAndGet();
        });

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(ZMQ.PUB);
        subscriber.bind(thread.url);
        Thread.sleep(400); // Connection to be established

        subscriber.send(".#HB.UAT.kernel.*.0;status=DOWN;quickfixPersistence=ORACLE");
        System.out.println("Sent 1");
        Thread.sleep(1400);
        subscriber.send(".#HB.UAT.kernel.*.0;status=LIVE;quickfixPersistence=ORACLE");
        System.out.println("Sent 2");
        Thread.sleep(200);

        subscriber.close();
        assertThat(counter.get()).isEqualTo(2);

    }
}