package org.neo.gomina.model.scm;

import org.neo.gomina.model.scm.model.Commit;
import org.neo.gomina.model.scm.model.ScmDetails;

import java.util.List;

public interface ScmConnector {

    ScmDetails getSvnDetails(String svnRepo, String svnUrl);
    List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception;

}
