package org.neo.gomina.model.scminfo.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.maven.MavenUtils;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

public class DefaultScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(DefaultScmConnector.class);

    private ScmRepos scmRepos;

    @Inject
    public DefaultScmConnector(ScmRepos scmRepos) {
        this.scmRepos = scmRepos;
    }

    @Override
    public ScmDetails getSvnDetails(String svnRepo, String svnUrl) {
        logger.info("Svn Details for " + svnUrl);
        ScmClient scmClient = scmRepos.get(svnRepo);
        ScmDetails scmDetails = new ScmDetails();
        try {
            String pom = scmClient.getFile(svnUrl + "/trunk/pom.xml", "-1");
            String currentVersion = MavenUtils.extractVersion(pom);

            List<Commit> logEntries = scmClient.getLog(svnUrl, "0", 100);
            Commit latestCommit = logEntries.size() > 0 ? logEntries.get(0) : null;
            String latestRevision = latestCommit != null ? latestCommit.revision : null;

            String lastReleasedRev = getLastReleaseRev(logEntries);
            String lastReleasePrepRev = getLastReleasePrepRev(logEntries);
            String lastReleasedVersion = null;
            if (lastReleasePrepRev != null) {
                String lastReleasePom = scmClient.getFile(svnUrl + "/trunk/pom.xml", lastReleasePrepRev);
                lastReleasedVersion = MavenUtils.extractVersion(lastReleasePom);
            }

            scmDetails.url = svnUrl;
            scmDetails.latest = currentVersion;
            scmDetails.latestRevision = latestRevision;
            scmDetails.released = lastReleasedVersion;
            scmDetails.releasedRevision = lastReleasedRev;
            scmDetails.changes = commitCountTo(logEntries, lastReleasedRev); //diff.size();
        }
        catch (Exception e) {
            logger.error("Cannot get SVN information for " + svnUrl, e);
        }

        logger.info(scmDetails);
        return scmDetails;
    }

    private Integer commitCountTo(List<Commit> logEntries, String refRev) {
        int count = 0;
        for (Commit logEntry : logEntries) {
            if (StringUtils.equals(logEntry.revision, refRev)) {
                return count;
            }
            count++;
        }
        return null;
    }

    private String getLastReleaseRev(Collection<Commit> logEntries) throws Exception {
        for (Commit logEntry : logEntries) {
            if (StringUtils.startsWith(logEntry.message, "[maven-release-plugin]")) {
                return logEntry.revision;
            }
        }
        return null;
    }

    private String getLastReleasePrepRev(Collection<Commit> logEntries) {
        for (Commit logEntry : logEntries) {
            if (StringUtils.startsWith(logEntry.message, "[maven-release-plugin] prepare release")) {
                return logEntry.revision;
            }
        }
        return null;
    }

    @Override
    public List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception {
        ScmClient scmClient = scmRepos.get(svnRepo);
        return scmClient.getLog(svnUrl, "0", 100);
    }

}
