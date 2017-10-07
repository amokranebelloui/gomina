package org.neo.gomina.model.monitoring.zmq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.monitoring.Monitoring;
import org.zeromq.ZMQ;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZmqMonitorThread extends Thread {

    private final static Logger logger = LogManager.getLogger(ZmqMonitorThread.class);

    private Monitoring monitoring;

    private String url;
    private List<String> subscriptions;

    public ZmqMonitorThread(Monitoring monitoring, String url, List<String> subscriptions) {
        this.monitoring = monitoring;
        this.url = url;
        this.subscriptions = subscriptions;
    }

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
        subscriber.connect(url);
        for (String subscription : subscriptions) {
            subscriber.subscribe(subscription.getBytes());
        }
        logger.info("Listening to " + url);

        while (!Thread.currentThread().isInterrupted ()) {
            String obj = subscriber.recvStr(0);
            logger.trace("Received " + obj);

            try {
                Map<String, Object> indicators = parse(obj);
                enrich(indicators);
                monitoring.notify(
                        (String)indicators.get("@env"),
                        (String)indicators.get("@instanceId"),
                        indicators);
                logger.trace(indicators);
            }
            catch (Exception e) {
                logger.error("", e);
            }
        }
        subscriber.close();
        context.term();
        logger.info("closed");
    }

    private void enrich(Map<String, Object> indicators) {
        indicators.put("TIMESTAMP", new Date());
        indicators.put("STATUS", mapStatus((String)indicators.get("STATUS")));
    }

    private String mapStatus(String status) {
        return "SHUTDOWN".equals(status) ? "DOWN" : status;
    }

    private Map<String, Object> parse(String obj) {
        int i = obj.indexOf(";");
        String[] header = obj.substring(0, i).split("\\.");
        String env = header[2];
        String instanceId = header[3];
        String body = obj.substring(i + 1);
        Map<String, Object> indicators = mapBody(body);
        indicators.put("@env", env);
        indicators.put("@instanceId", instanceId);
        return indicators;
    }

    private Map<String, Object> mapBody(String body) {
        Map<String, Object> map = new HashMap<>();
        for (String keyValue : body.split(";")) {
            String[] keyValueSplit = keyValue.split("=");
            map.put(keyValueSplit[0], keyValueSplit[1]);
        }
        return map;
    }

}
