package org.neo.gomina.model.scm.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmConfig;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scm.dummy.DummyScmClient;
import org.neo.gomina.model.scm.svn.TmateSoftSvnClient;
import org.neo.gomina.model.security.Passwords;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ConfigScmRepos implements ScmRepos {

    private final static Logger logger = LogManager.getLogger(ConfigScmRepos.class);

    private Map<String, ScmClient> clients = new HashMap<>();

    @Inject
    public ConfigScmRepos(ScmConfig config, Passwords passwords) {
        for (ScmConfig.ScmRepo repo : config.repos) {
            try {
                ScmClient client = buildScmClient(repo, passwords);
                clients.put(repo.id, client);
                logger.info("Added {} {}", repo.id, client);
            }
            catch (Exception e) {
                logger.info("Cannot build SCM client for " + repo.id, e);
            }
        }
    }

    @Override
    public ScmClient get(String id) {
        return clients.get(id);
    }

    private ScmClient buildScmClient(ScmConfig.ScmRepo repo, Passwords passwords) throws Exception {
        switch (repo.type) {
            case "svn":
                return new TmateSoftSvnClient(repo.location,
                    repo.username, passwords.getRealPassword(repo.passwordAlias));
            case "dummy":
                return new DummyScmClient();
        }
        return null;
    }
}
