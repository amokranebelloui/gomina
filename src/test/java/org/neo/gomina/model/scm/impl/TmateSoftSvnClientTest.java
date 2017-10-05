package org.neo.gomina.model.scm.impl;

import org.junit.Test;
import org.neo.gomina.model.scm.model.Commit;

import java.util.List;

public class TmateSoftSvnClientTest {


    @Test
    public void testSvn() throws Exception {
        TmateSoftSvnClient svnClient = new TmateSoftSvnClient("file:////Users/Amokrane/Work/SvnRepo/svn-repo-demo");

        List<Commit> svnLog = svnClient.getLog("svn-project2", "0", 100);
        for (Commit commit : svnLog) {
            System.out.println(commit);
        }
    }

}