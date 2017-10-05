package org.neo.gomina.model.scm;

import org.junit.Test;

public class ScmRepoRepositoryTest {

    @Test
    public void testRepo() throws Exception {
        ScmRepoRepository repos = new ScmRepoRepository();
        ScmClient scmClient = repos.get("home");
        System.out.println(scmClient);
    }
}