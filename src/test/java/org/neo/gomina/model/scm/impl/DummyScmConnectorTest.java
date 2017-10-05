package org.neo.gomina.model.scm.impl;

import org.junit.Test;
import org.neo.gomina.model.scm.model.Commit;
import org.neo.gomina.model.scm.model.ScmDetails;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class DummyScmConnectorTest {
    @Test
    public void getSvnDetails() throws Exception {
        DummyScmConnector svnRepository = new DummyScmConnector();

        ScmDetails detail = svnRepository.getSvnDetails("basket");
        assertThat(detail).isNotNull();
        assertThat(detail.changes).isGreaterThan(2);

        ScmDetails none = svnRepository.getSvnDetails("unknown");
        assertThat(none).isNull();
    }

    @Test
    public void testCommitLog() throws Exception {
        DummyScmConnector svnRepository = new DummyScmConnector();
        List<Commit> commitLog = svnRepository.getCommitLog("basket");
        assertThat(commitLog).isNotNull();
        assertThat(commitLog.size()).isGreaterThan(5);
    }
}