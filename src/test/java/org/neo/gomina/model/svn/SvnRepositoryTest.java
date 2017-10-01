package org.neo.gomina.model.svn;

import org.junit.Test;

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

}