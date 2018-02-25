package org.neo.gomina.model.scminfo.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.maven.MavenUtils;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scm.MavenReleaseFlagger;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(DefaultScmConnector.class);

    private ScmRepos scmRepos;

    @Inject
    public DefaultScmConnector(ScmRepos scmRepos) {
        this.scmRepos = scmRepos;
    }

    @Override
    public void refresh(String svnRepo, String svnUrl) {

    }

    @Override
    public ScmDetails getSvnDetails(String svnRepo, String svnUrl) {
        logger.info("Svn Details for " + svnUrl);
        ScmClient scmClient = scmRepos.get(svnRepo);
        ScmDetails scmDetails = new ScmDetails();
        try {
            String pom = scmClient.getFile(svnUrl + "/trunk/pom.xml", "-1");
            String currentVersion = MavenUtils.extractVersion(pom);

            List<Commit> logEntries = getCommits(svnRepo, svnUrl, scmClient);

            scmDetails.url = svnUrl;
            scmDetails.latest = currentVersion;

            Commit latestCommit = logEntries.size() > 0 ? logEntries.get(0) : null;
            String latestRevision = latestCommit != null ? latestCommit.getRevision() : null;
            scmDetails.latestRevision = latestRevision;

            String lastReleasedVersion = logEntries.stream()
                    .filter(commit1 -> StringUtils.isNotBlank(commit1.getRelease()))
                    .findFirst()
                    .map(commit -> commit.getRelease()).orElse(null);
            scmDetails.released = lastReleasedVersion;

            //String lastReleasedRev = getLastReleaseRev(logEntries);
            String lastReleasedRev = logEntries.stream()
                    .filter(commit1 -> StringUtils.isNotBlank(commit1.getNewVersion()))
                    .findFirst()
                    .map(commit -> commit.getRevision()).orElse(null);
            scmDetails.releasedRevision = lastReleasedRev;
            
            scmDetails.changes = commitCountTo(logEntries, lastReleasedRev); //diff.size();
        }
        catch (Exception e) {
            logger.error("Cannot get SVN information for " + svnUrl, e);
        }

        logger.info(scmDetails);
        return scmDetails;
    }

    protected List<Commit> getCommits(String svnRepo, String svnUrl, ScmClient scmClient) throws Exception {
        MavenReleaseFlagger mavenReleaseFlagger = new MavenReleaseFlagger(scmClient, svnUrl);
        return scmClient.getLog(svnUrl, "0", 100).stream()
                .map(mavenReleaseFlagger::flag)
                .collect(Collectors.toList());
    }

    private Integer commitCountTo(List<Commit> logEntries, String refRev) {
        int count = 0;
        for (Commit logEntry : logEntries) {
            if (StringUtils.equals(logEntry.getRevision(), refRev)) {
                return count;
            }
            count++;
        }
        return null;
    }

    @Override
    public List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception {
        try {
            ScmClient scmClient = scmRepos.get(svnRepo);
            return scmClient.getLog(svnUrl, "0", 100);
        }
        catch (Exception e) {
            logger.error("Cannot get commit log: " + svnRepo + " " + svnUrl);
            return new ArrayList<>();
        }
    }

}
