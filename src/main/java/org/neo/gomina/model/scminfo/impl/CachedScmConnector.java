package org.neo.gomina.model.scminfo.impl;

import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(CachedScmConnector.class);

    private ScmConnector scmConnector;

    private Map<String, ScmDetails> cache = new HashMap<>();

    private XStream xStream = new XStream();
    private File svnProjectsCache = new File("svnprojects.cache");

    @Inject
    public CachedScmConnector(DefaultScmConnector scmConnector) {
        this.scmConnector = scmConnector;

        File file = new File(".cache");
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            logger.info("Created " + file + " " + mkdir);
        }
    }

    @Override
    public void refresh(String svnRepo, String svnUrl) {
        // FIXME Refresh SCM
    }

    @Override
    public ScmDetails getSvnDetails(String svnRepo, String svnUrl) {
        String fileName = svnRepo + "-" + svnUrl.replaceAll("/", "-").replaceAll("\\\\", "-");
        File cacheFile = new File(".cache/" + fileName);
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
                scmDetails = getFromScmAndCache(svnRepo, svnUrl, cacheFile);
            }
        }
        else {
            scmDetails = getFromScmAndCache(svnRepo, svnUrl, cacheFile);
            logger.info("SCM Detail Served from SCM " + scmDetails);
        }
        return scmDetails != null ? scmDetails : new ScmDetails();
    }

    private ScmDetails getFromScmAndCache(String svnRepo, String svnUrl, File cacheFile) {
        ScmDetails scmDetails;
        scmDetails = scmConnector.getSvnDetails(svnRepo, svnUrl);
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
    public List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception {
        logger.info("Commit Log Served from SCM");
        return scmConnector.getCommitLog(svnRepo, svnUrl);
    }

}
