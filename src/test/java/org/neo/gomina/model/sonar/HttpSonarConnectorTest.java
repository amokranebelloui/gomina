package org.neo.gomina.model.sonar;

import org.junit.Test;
import org.neo.gomina.model.sonar.http.HttpSonarConnector;

public class HttpSonarConnectorTest {

    @Test
    public void getMetrics() throws Exception {
        HttpSonarConnector httpSonarConnector = new HttpSonarConnector("http://localhost:9000");
        System.out.println(httpSonarConnector.getMetrics(null));
        System.out.println(httpSonarConnector.getMetrics("torkjell:torkjell"));
        System.out.println(httpSonarConnector.getMetrics("torkjell:unknown"));
    }

}