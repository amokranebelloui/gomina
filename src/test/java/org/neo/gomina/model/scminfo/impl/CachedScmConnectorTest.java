package org.neo.gomina.model.scminfo.impl;

import org.junit.Test;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scm.dummy.DummyScmClient;

public class CachedScmConnectorTest {

    @Test
    public void getSvnDetails() {
        class FileScmReposOverride implements ScmRepos {
            public ScmClient get(String id) {
                return new DummyScmClient();
            }
        }

        CachedScmConnector connector = new CachedScmConnector(new FileScmReposOverride());

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");
        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");
        connector.getSvnDetails("repo", "OMS/Server/tradex-basketmanager");
    }

}