package org.neo.gomina.model.scm.impl;

import org.junit.Test;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepoRepository;

public class DefaultScmConnectorTest {
    @Test
    public void getSvnDetails() throws Exception {
        //DefaultScmConnector connector = new DefaultScmConnector(new TmateSoftSvnClient());

        class ScmRepoRepositoryOverride extends ScmRepoRepository {

            public ScmRepoRepositoryOverride() throws Exception {
            }

            @Override
            public ScmClient get(String id) {
                return new DummyScmClient();
            }
        }

        DefaultScmConnector connector = new DefaultScmConnector(new ScmRepoRepositoryOverride());

        connector.getSvnDetails("svn-project1");
        connector.getSvnDetails("svn-project2");

    }

}