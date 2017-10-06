package org.neo.gomina.model.scm;

import org.junit.Test;
import org.neo.gomina.model.scm.file.FileScmRepos;

public class FileScmReposTest {

    @Test
    public void testRepo() throws Exception {
        ScmRepos repos = new FileScmRepos();
        ScmClient scmClient = repos.get("home");
        System.out.println(scmClient);
    }
}