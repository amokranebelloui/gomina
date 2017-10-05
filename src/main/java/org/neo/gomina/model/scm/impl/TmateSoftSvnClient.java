package org.neo.gomina.model.scm.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.model.Commit;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TmateSoftSvnClient implements ScmClient {

    private final static Logger logger = LogManager.getLogger(TmateSoftSvnClient.class);

    //String url = "http://svn/actions/venteaction/vaction/Developpement/";
    private String url = "file:////Users/Amokrane/Work/SvnRepo/svn-repo-demo";    // FIXME Configurable

    private SVNRepository repository = null;

    public TmateSoftSvnClient() throws SVNException {
        DAVRepositoryFactory.setup();
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        /*
        if (auth != null) {
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(auth.getUser(), auth.getPassword());
            repository.setAuthenticationManager(authManager);
        }
        */
    }

    @Override
    public List<Commit> getLog(String url, long rev, int count) throws Exception {
        List<Commit> result = new ArrayList<>();
        final Collection<SVNLogEntry> logEntries = new ArrayList<>();
        repository.log(new String[]{url + "/trunk"}, -1, rev, true, true, count, logEntries::add);
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

}
