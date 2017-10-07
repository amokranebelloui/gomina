package org.neo.gomina.model.scminfo;

import org.junit.Test;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.file.FileScmRepos;
import org.neo.gomina.model.scm.dummy.DummyScmClient;
import org.neo.gomina.model.scminfo.impl.DefaultScmConnector;

public class DefaultScmConnectorTest {
    @Test
    public void getSvnDetails() throws Exception {
        //DefaultScmConnector connector = new DefaultScmConnector(new TmateSoftSvnClient());

        class FileScmReposOverride extends FileScmRepos {

            public FileScmReposOverride() throws Exception {
            }

            @Override
            public ScmClient get(String id) {
                return new DummyScmClient();
            }
        }

        DefaultScmConnector connector = new DefaultScmConnector(new FileScmReposOverride());

        connector.getSvnDetails("repo", "svn-project1");
        connector.getSvnDetails("repo", "svn-project2");

    }

}