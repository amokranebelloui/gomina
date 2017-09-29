package org.neo.gomina.model.sonar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SonarClient {

    private static final Logger logger = LogManager.getLogger(SonarClient.class);

    private ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println(new SonarClient().getMetrics("http://localhost:9000", null));
        System.out.println(new SonarClient().getMetrics("http://localhost:9000", "torkjell:torkjell"));
        System.out.println(new SonarClient().getMetrics("http://localhost:9000", "torkjell:unknown"));
    }

    public Map<String, SonarIndicators> getMetrics(String root, String resource) {
        Map<String, SonarIndicators> map = new HashMap<>();
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            //resource=x.oms.facilitation:tradex-facilitation&
            String resourceQuery = StringUtils.isNotBlank(resource) ? "resource=" + resource + "&" : "";
            HttpGet httpGet = new HttpGet(root + "/api/resources?" + resourceQuery + "metrics=ncloc,coverage");
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                logger.info("-> Result " + response1.getStatusLine());
                if (response1.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity1 = response1.getEntity();

                    List<Map<String, Object>> data = mapper.readValue(entity1.getContent(), List.class);
                    for (Map<String, Object> project : data) {
                        String key = (String)project.get("key");
                        List<Map<String, String>> msr = (List<Map<String, String>>)project.get("msr");
                        Double ncloc = (Double)getMetric(msr, "ncloc");
                        Double coverage = (Double)getMetric(msr, "coverage");
                        logger.info("-> Data " + key + " " + ncloc + " " + coverage);
                        map.put(key, new SonarIndicators(ncloc, coverage));
                    }
                    EntityUtils.consume(entity1);
                }
            }
            finally {
                response1.close();
            }
        }
        catch (IOException e) {
            logger.error("", e);
        }
        return map;
    }

    public Object getMetric(List<Map<String, String>> msr, String metric) {
        if (msr != null) {
            for (Map<String, String> m : msr) {
                if (metric != null && metric.equals(m.get("key"))) {
                    return m.get("val");
                }
            }
        }
        return null;
    }

}
