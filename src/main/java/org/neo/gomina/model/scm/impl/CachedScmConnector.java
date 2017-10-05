package org.neo.gomina.model.scm.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.ScmConnector;
import org.neo.gomina.model.scm.model.Commit;
import org.neo.gomina.model.scm.model.ScmDetails;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(CachedScmConnector.class);

    private ScmConnector scmConnector;

    private Map<String, ScmDetails> cache = new HashMap<>();

    @Inject
    public CachedScmConnector(DefaultScmConnector scmConnector) {
        this.scmConnector = scmConnector;
    }

    @Override
    public ScmDetails getSvnDetails(String svnRepo, String svnUrl) {
        ScmDetails scmDetails;
        if (cache.containsKey(svnUrl)) {
            scmDetails = cache.get(svnUrl);
            logger.info("SCM Detail Served from Cache");
        }
        else {
            scmDetails = scmConnector.getSvnDetails(svnRepo, svnUrl);
            cache.put(svnUrl, scmDetails);
            logger.info("SCM Detail Served from SCM");
        }
        return scmDetails;
    }

    @Override
    public List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception {
        logger.info("Commit Log Served from SCM");
        return scmConnector.getCommitLog(svnRepo, svnUrl);
    }

}
