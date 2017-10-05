package org.neo.gomina.model.scm.impl;

import org.junit.Test;

public class DefaultScmConnectorTest {
    @Test
    public void getSvnDetails() throws Exception {
        //DefaultScmConnector connector = new DefaultScmConnector(new TmateSoftSvnClient());
        DefaultScmConnector connector = new DefaultScmConnector(new DummyScmClient());

        connector.getSvnDetails("svn-project1");
        connector.getSvnDetails("svn-project2");

    }

}