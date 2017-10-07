package org.neo.gomina.model.monitoring.zmq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.monitoring.Monitoring;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZmqMonitorThread extends Thread {

    private final static Logger logger = LogManager.getLogger(ZmqMonitorThread.class);

    private Monitoring monitoring;

    // FIXME Configurable
    private List<String> envs;
    public String url = "tcp://localhost:7070";

    @Inject
    public ZmqMonitorThread(Monitoring monitoring, Inventory inventory) {
        this.monitoring = monitoring;
        envs = inventory.getEnvironments().stream()
                .map(e -> e.id)
                .collect(Collectors.toList());
    }

    @Inject
    public void init() {
        // Get conf
        this.start();
    }

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
        subscriber.connect(url);
        for (String env : envs) {
            subscriber.subscribe((".#HB." + env + ".").getBytes());
        }
        logger.info("Listening to " + url);

        while (!Thread.currentThread().isInterrupted ()) {
            String obj = subscriber.recvStr(0);
            logger.trace("Received " + obj);

            try {
                int i = obj.indexOf(";");
                String[] header = obj.substring(0, i).split("\\.");
                String env = header[2];
                String instanceId = header[3];
                String body = obj.substring(i + 1);
                Map<String, Object> indicators = mapBody(body);

                indicators.put("TIMESTAMP", new Date());
                indicators.put("STATUS", mapStatus((String)indicators.get("STATUS")));
                monitoring.notify(env, instanceId, indicators);

                logger.trace(instanceId + " " + env + " " + indicators);
            }
            catch (Exception e) {
                logger.error("", e);
            }
        }
        subscriber.close();
        context.term();
        logger.info("closed");
    }

    private Map<String, Object> mapBody(String body) {
        Map<String, Object> map = new HashMap<>();
        for (String keyValue : body.split(";")) {
            String[] keyValueSplit = keyValue.split("=");
            map.put(keyValueSplit[0], keyValueSplit[1]);
        }
        return map;
    }

    private String mapStatus(String status) {
        return "SHUTDOWN".equals(status) ? "DOWN" : status;
    }

}
