package org.neo.gomina.model.svn;

import org.junit.Test;

import java.util.List;

public class SvnClientTest {


    @Test
    public void testSvn() throws Exception {
        SvnClient svnClient = new SvnClient();

        List<Commit> svnLog = svnClient.getSvnLog("svn-project1", 0, true);
        for (Commit commit : svnLog) {
            System.out.println(commit);
        }
    }

    @Test
    public void testAnalyze() throws Exception {
        SvnClient svnClient = new SvnClient();

        SvnDetails details = svnClient.analyze("svn-project1", "svn-project1");
        System.out.println(details);
    }
}