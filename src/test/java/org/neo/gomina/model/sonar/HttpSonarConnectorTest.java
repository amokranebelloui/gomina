package org.neo.gomina.model.sonar;

import org.junit.Test;
import org.neo.gomina.model.sonar.http.HttpSonarConnector;

public class HttpSonarConnectorTest {

    @Test
    public void getMetrics() throws Exception {
        System.out.println(new HttpSonarConnector().getMetrics(null));
        System.out.println(new HttpSonarConnector().getMetrics("torkjell:torkjell"));
        System.out.println(new HttpSonarConnector().getMetrics("torkjell:unknown"));
    }

}