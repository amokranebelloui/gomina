package org.neo.gomina.model.issues

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class IssuesProjectsTest {
    @Test
    fun testPattern() {

        val issues = IssueProjects().apply { init(listOf("DEMO", "TEST", "SUPPORT")) }
        assertThat("[DEMO-668] Some commit [DEMO-778]".extractIssues(issues)) .containsOnly("DEMO-668", "DEMO-778")
        assertThat("SUPPORT-040: Some commit for bugfixing".extractIssues(issues)) .containsOnly("SUPPORT-040")
        assertThat("SUPPORT041: Some commit for bugfixing".extractIssues(issues)) .containsOnly()
        assertThat("DEMO-668 -DEMO-660- Some DF-12 commit".extractIssues(issues)) .containsOnly("DEMO-668", "DEMO-660")
    }
}