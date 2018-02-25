package org.neo.gomina.model.scm.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scm.dummy.DummyScmClient;

import java.util.List;

public class DummyScmClientTest {

    private final static Logger logger = LogManager.getLogger(DummyScmClientTest.class);

    @Test
    public void getLog() throws Exception {
        DummyScmClient client = new DummyScmClient();
        List<Commit> log = client.getLog("OMS/Server/tradex-basketmanager", "0", 100);
        for (Commit commit : log) {
            logger.info(commit);
        }
    }

    @Test
    public void getFile() throws Exception {
        DummyScmClient client = new DummyScmClient();
        String file = client.getFile("OMS/Server/tradex-basketmanager", "-1");
        logger.info(file);
    }

}