package org.neo.gomina.model.scm;

import java.util.List;

public interface ScmClient {

    /**
     * Get log from HEAD to revision, max @count elements
     */
    List<Commit> getLog(String url, String rev, int count) throws Exception;

    /**
     * get file for a revision, HEAD is -1
     */
    String getFile(String url, String rev) throws Exception;

}
