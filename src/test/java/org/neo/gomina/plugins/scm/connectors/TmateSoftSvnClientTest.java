package org.neo.gomina.plugins.scm.connectors;

import org.junit.Test;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.plugins.scm.connectors.TmateSoftSvnClient;

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