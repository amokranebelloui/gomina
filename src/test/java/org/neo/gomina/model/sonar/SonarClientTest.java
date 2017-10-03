package org.neo.gomina.model.sonar;

import org.junit.Test;

public class SonarClientTest {

    @Test
    public void getMetrics() throws Exception {
        System.out.println(new SonarClient().getMetrics(null));
        System.out.println(new SonarClient().getMetrics("torkjell:torkjell"));
        System.out.println(new SonarClient().getMetrics("torkjell:unknown"));
    }

}