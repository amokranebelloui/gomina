package org.neo.gomina.plugins.scm.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepo;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.plugins.scm.ScmPlugin;
import org.neo.gomina.plugins.scm.connectors.DummyScmClient;

public class ScmPluginTest {

    @Test
    public void getSvnDetails() {
        class FileScmReposOverride implements ScmRepos {
            public ScmClient getClient(String id) {
                return new DummyScmClient();
            }

            public ScmRepo getRepo(@NotNull String id) {
                return null;
            }
        }

        ScmPlugin connector = new ScmPlugin(new FileScmReposOverride());

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");
        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");

        connector.getSvnDetails("repo", "OMS/Server/tradex-fixin");
        connector.getSvnDetails("repo", "OMS/Server/tradex-basketmanager");
    }

}