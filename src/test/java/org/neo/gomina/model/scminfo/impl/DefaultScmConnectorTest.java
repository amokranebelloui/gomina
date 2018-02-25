package org.neo.gomina.model.scminfo.impl;

import org.junit.Test;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scm.dummy.DummyScmClient;

public class DefaultScmConnectorTest {
    @Test
    public void getSvnDetails() throws Exception {
        //DefaultScmConnector connector = new DefaultScmConnector(new TmateSoftSvnClient());

        class FileScmReposOverride implements ScmRepos {
            public ScmClient get(String id) {
                return new DummyScmClient();
            }
        }

        DefaultScmConnector connector = new DefaultScmConnector(new FileScmReposOverride());

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");
        connector.getSvnDetails("repo", "OMS/Server/tradex-basketmanager");

    }

}