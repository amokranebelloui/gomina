package org.neo.gomina.model.scm.svn;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scm.ScmClient;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TmateSoftSvnClient implements ScmClient {

    private final static Logger logger = LogManager.getLogger(TmateSoftSvnClient.class);

    private String url;
    private SVNRepository repository = null;

    public TmateSoftSvnClient(String url) throws SVNException {
        this(url, null, null);
    }

    public TmateSoftSvnClient(String url, String username, String password) throws SVNException {
        this.url = url;
        DAVRepositoryFactory.setup();
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));

        if (StringUtils.isNotBlank(username)) {
            logger.info("Connecting to {}, using {}/***{}", url, username, StringUtils.length(password));
            repository.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(username, password));
        }
    }

    @Override
    public List<Commit> getLog(String url, String rev, int count) throws Exception {
        List<Commit> result = new ArrayList<>();
        final Collection<SVNLogEntry> logEntries = new ArrayList<>();
        repository.log(new String[]{url + "/trunk"}, -1, Long.valueOf(rev), true, true, count, logEntries::add);
        for (SVNLogEntry logEntry : logEntries) {
            Commit svnLogItem = new Commit();
            svnLogItem.revision = revAsString(logEntry.getRevision());
            svnLogItem.date = logEntry.getDate();
            svnLogItem.author = logEntry.getAuthor();
            svnLogItem.message = StringUtils.replaceChars(logEntry.getMessage(), "\n", " ");
            result.add(svnLogItem);
        }
        return result;
    }

    @Override
    public String getFile(String url, String rev) throws SVNException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        repository.getFile(url, Long.valueOf(rev), new SVNProperties(), baos);
        return new String(baos.toByteArray());
    }

    private String revAsString(Long rev) {
        return rev != null ? String.valueOf(rev) : null;
    }

    @Override
    public String toString() {
        return String.format("TmateSoftSvnClient{url='%s'}", url);
    }
}
