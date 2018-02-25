package org.neo.gomina.model.scminfo;

import org.neo.gomina.model.scm.Commit;

import java.util.List;

public interface ScmConnector {

    void refresh(String svnRepo, String svnUrl);
    ScmDetails getSvnDetails(String svnRepo, String svnUrl);
    List<Commit> getCommitLog(String svnRepo, String svnUrl) throws Exception;

}
