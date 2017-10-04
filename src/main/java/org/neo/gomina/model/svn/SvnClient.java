package org.neo.gomina.model.svn;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SvnClient {

    private final static Logger logger = LogManager.getLogger(SvnClient.class);

    //String url = "http://svn/actions/venteaction/vaction/Developpement/";
    String url = "file:////Users/Amokrane/Work/SvnRepo/svn-repo-demo";    // FIXME Configurable

    private SVNRepository repository = null;

    public SvnClient() throws SVNException {
        DAVRepositoryFactory.setup();

        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        //Authentication authentication = new Authentication();
        //Auth auth = authentication.getSvn();
        /*
        if (auth != null) {
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(auth.getUser(), auth.getPassword());
            repository.setAuthenticationManager(authManager);
        }
        */
    }

    public SvnDetails analyze(String projectId, String projectUrl) throws Exception {
        //String projectId = projectLocation;
        //String projectUrl = projectLocation.replace('|', '/');
        //String projectId = aProjectId.getId();
        logger.info("Analyzing " + projectUrl);


        SVNProperties rootDirFileProperties = new SVNProperties();
        //getFile(projectUrl + "/trunk", -1, repository, rootDirFileProperties);
        String latestRev = rootDirFileProperties.getStringValue("svn:entry:committed-rev");
        long latestRevision;
        try {
            latestRevision = Long.parseLong(latestRev);
        } catch (NumberFormatException e) {
            latestRevision = -1;
            //e.printStackTrace();
        }

        SVNProperties pomFileProperties = new SVNProperties();
        String pom = getFile(projectUrl + "/trunk/pom.xml", -1, repository, pomFileProperties);
        String currentVersion = MavenUtils.extractVersion(pom);

        //Collection<SVNLogEntry> logEntries1 = repository.log(new String[]{projectUrl + "/trunk"}, null, startRevision, endRevision, true, true);

        final Collection<SVNLogEntry> logEntries = getSvnLogEntries(projectUrl, 100);
        long lastReleasedRevision = getLastReleasedRevision(logEntries);
        String lastReleasedVersion = getLastReleasedVersion(projectUrl, logEntries);

        List<Commit> diff = getSvnLog(projectUrl, lastReleasedRevision, false);
        logger.info(projectUrl + ":\t " + currentVersion + "(" + latestRevision + ")/" + lastReleasedVersion + "(" + lastReleasedRevision + ") Diff=" + diff.size());
        /**
         for (String d : diff) {
         System.out.println("    " + d);
         }
         /**/

        SvnDetails svnProject = new SvnDetails();
        svnProject.id = projectId;
        svnProject.latest = currentVersion;
        //svnProject.latestRevision = latestRevision; // FIXME +
        svnProject.released = lastReleasedVersion;
        //svnProject.releasedRevision = lastReleasedRevision; // FIXME +
        svnProject.changes = diff.size();
        ///this.update(projectId, svnProject); // FIXME +

        return svnProject;
    }

    private Collection<SVNLogEntry> getSvnLogEntries(String projectUrl, int count) throws SVNException {
        final Collection<SVNLogEntry> logEntries = new ArrayList<>();
        long startRevision = 0;
        long endRevision = -1; //HEAD (the latest) revision
        repository.log(new String[]{projectUrl + "/trunk"}, endRevision, startRevision, true, true, count, new ISVNLogEntryHandler() {
            @Override public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                logEntries.add(logEntry);
            }
        });
        return logEntries;
    }

    private long getLastReleasedRevision(Collection<SVNLogEntry> logEntries) throws Exception {
        //long lastReleaseRev = 0;
        for (SVNLogEntry logEntry : logEntries) {
            if (StringUtils.startsWith(logEntry.getMessage(), "[maven-release-plugin]")) {
                //lastReleaseRev = logEntry.getRevision();
                return logEntry.getRevision();
            }
        }
        //return lastReleaseRev;
        return 0;
    }

    private String getLastReleasedVersion(String project, Collection<SVNLogEntry> logEntries) throws Exception {
        long lastReleaseRev = 0;
        for (SVNLogEntry logEntry : logEntries) {
            if (StringUtils.startsWith(logEntry.getMessage(), "[maven-release-plugin] prepare release")) {
                lastReleaseRev = logEntry.getRevision();
                break;
            }
        }
        if (lastReleaseRev != 0) {
            SVNProperties pomFileProperties = new SVNProperties();
            String lastReleasePom = getFile(project + "/trunk/pom.xml", lastReleaseRev, repository, pomFileProperties);
            String lastReleaseVersion = MavenUtils.extractVersion(lastReleasePom);
            return lastReleaseVersion;
        }
        else {
            return "-1";
        }
    }

    public List<Commit> getSvnLog(String project, long startRevision, boolean showAll) throws Exception {
        //project = project.replace('|', '/');
        List<Commit> result = new ArrayList<>();
        long endRevision = -1; //HEAD (the latest) revision
        final Collection<SVNLogEntry> logEntries = new ArrayList<>();
        repository.log(new String[]{project + "/trunk"}, endRevision, startRevision, true, false, 100, new ISVNLogEntryHandler() {
            @Override public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                logEntries.add(logEntry);
            }
        });
        //Collection<SVNLogEntry> logEntries1 = repository.log(new String[]{project + "/trunk"}, null, startRevision, endRevision, true, false);
        boolean first = true;
        for (SVNLogEntry logEntry : logEntries) {
            if (!first || showAll) {
                Commit svnLogItem = new Commit();
                svnLogItem.revision = String.valueOf(logEntry.getRevision());
                //svnLogItem.date = new SimpleDateFormat("yyyy-MM-dd").format(logEntry.getDate());
                svnLogItem.date = logEntry.getDate();
                svnLogItem.author = logEntry.getAuthor();
                svnLogItem.message = StringUtils.replaceChars(logEntry.getMessage(), "\n", " ");
                result.add(svnLogItem);
            }
            first = false;
        }
        // FIXME Sorting
        /*
        Collections.sort(result, new Comparator<Commit>() {
            @Override public int compare(Commit o1, Commit o2) {
                return o1.revision < o2.revision ? 1 : -1;
            }
        });
        */
        return result;
    }

    private String getFile(String file, long rev, SVNRepository repository, SVNProperties fileProperties) throws SVNException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        repository.getFile(file, rev, fileProperties, baos);
        return new String(baos.toByteArray());
    }

}
