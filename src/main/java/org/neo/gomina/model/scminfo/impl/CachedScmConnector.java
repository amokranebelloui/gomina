package org.neo.gomina.model.scminfo.impl;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scm.MavenReleaseFlagger;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.ScmRepos;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CachedScmConnector extends DefaultScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(CachedScmConnector.class);

    //private ScmConnector scmConnector;

    private Map<String, ScmDetails> cache = new HashMap<>();

    private XStream xStream = new XStream();
    private File svnProjectsCache = new File("svnprojects.cache");


    @Inject
    public CachedScmConnector(/*DefaultScmConnector scmConnector*/ScmRepos scmRepos) {
        super(scmRepos);
        //this.scmConnector = scmConnector;

        File file = new File(".cache");
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            logger.info("Created " + file + " " + mkdir);
        }
    }

    private File getDetailFileCache(String svnRepo, String svnUrl) {
        String fileName = svnRepo + "-" + svnUrl.replaceAll("/", "-").replaceAll("\\\\", "-");
        return new File(".cache/" + fileName);
    }

    private File getLogCacheFile(String svnRepo, String svnUrl) {
        String fileName = svnRepo + "-" + svnUrl.replaceAll("/", "-").replaceAll("\\\\", "-");
        return new File(".cache/" + fileName + ".log");
    }

    @Override
    public void refresh(String svnRepo, String svnUrl) {
        // FIXME Refresh SCM
        getFromScmAndCache(svnRepo, svnUrl);
    }

    @Override
    public ScmDetails getSvnDetails(String svnRepo, String svnUrl) {
        File cacheFile = getDetailFileCache(svnRepo, svnUrl);
        ScmDetails scmDetails;
        if (cache.containsKey(svnUrl)) {
            scmDetails = cache.get(svnUrl);
            logger.info("SCM Detail Served from Memory Cache " + scmDetails);
        }
        else if (cacheFile.exists()) {
            try {
                scmDetails = (ScmDetails)xStream.fromXML(cacheFile);
                cache.put(svnUrl, scmDetails);
                logger.info("SCM Detail Served from File Cache " + scmDetails);
            }
            catch (Exception e) {
                logger.debug("Error loading cache", e);
                logger.info("Corrupted file, try to refresh...");
                scmDetails = getFromScmAndCache(svnRepo, svnUrl);
            }
        }
        else {
            scmDetails = getFromScmAndCache(svnRepo, svnUrl);
            logger.info("SCM Detail Served from SCM " + scmDetails);
        }
        return scmDetails != null ? scmDetails : new ScmDetails();
    }


    private ScmDetails getFromScmAndCache(String svnRepo, String svnUrl) {
        File cacheFile = getDetailFileCache(svnRepo, svnUrl);
        ScmDetails scmDetails;
        scmDetails = super.getSvnDetails(svnRepo, svnUrl);
        cache.put(svnUrl, scmDetails);
        try {
            xStream.toXML(scmDetails, new FileOutputStream(cacheFile));
        }
        catch (FileNotFoundException e) {
            logger.error("Saving cache for " + svnRepo + " " + svnUrl, e);
        }
        return scmDetails;
    }

    @Override
    protected List<Commit> getCommits(String svnRepo, String svnUrl, ScmClient scmClient) throws Exception {
        File cacheFile = getLogCacheFile(svnRepo, svnUrl);
        List<Commit> cached = new ArrayList<>();
        if (cacheFile.exists()) {
            cached = (List<Commit>)xStream.fromXML(cacheFile);
        }
        String lastKnown = cached.stream().findFirst().map(commit -> commit.getRevision()).orElse("0");

        MavenReleaseFlagger mavenReleaseFlagger = new MavenReleaseFlagger(scmClient, svnUrl);
        List<Commit> commits = scmClient.getLog(svnUrl, lastKnown, 100).stream()
                .map(mavenReleaseFlagger::flag)
                .collect(Collectors.toList());
        if (commits.size() > 0 && StringUtils.equals(commits.get(commits.size() - 1).getRevision(), lastKnown)) {
            commits.remove(commits.size() - 1); // To avoid duplicate
        }

        logger.info("Cached " + cached.size() + " " + cached);
        logger.info("Retrieved " + commits.size() + " " + commits);
        commits.addAll(cached);
        xStream.toXML(commits, new FileOutputStream(cacheFile));
        return commits;
    }

    @Override
    public List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception {
        logger.info("Commit Log Served from SCM");
        return super.getCommitLog(svnRepo, svnUrl);
    }

}
