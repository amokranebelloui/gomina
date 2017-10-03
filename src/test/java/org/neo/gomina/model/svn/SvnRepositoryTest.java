package org.neo.gomina.model.svn;

import org.junit.Test;
import org.neo.gomina.api.projects.CommitLogEntry;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class SvnRepositoryTest {
    @Test
    public void getSvnDetails() throws Exception {
        SvnRepository svnRepository = new SvnRepository();

        SvnDetails detail = svnRepository.getSvnDetails("basket");
        assertThat(detail).isNotNull();
        assertThat(detail.changes).isGreaterThan(2);

        SvnDetails none = svnRepository.getSvnDetails("unknown");
        assertThat(none).isNull();
    }

    @Test
    public void testCommitLog() throws Exception {
        SvnRepository svnRepository = new SvnRepository();
        List<CommitLogEntry> commitLog = svnRepository.getCommitLog("basket");
        assertThat(commitLog).isNotNull();
        assertThat(commitLog.size()).isGreaterThan(5);
    }
}