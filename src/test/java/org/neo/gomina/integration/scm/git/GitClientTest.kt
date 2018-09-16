package org.neo.gomina.integration.scm.git

import org.junit.Test
import org.neo.gomina.integration.scm.Trunk

class GitClientTest {

    @Test
    fun testGit() {
        val url = "/Users/Amokrane/Work/Code/Idea/gomina/.git"
        val client = GitClient(url)
        val log = client.getLog("useless", Trunk, "0", 100)
        log.forEach { println(it) }
        //Assertions.assertThat(log.size).isEqualTo(10)
    }

}