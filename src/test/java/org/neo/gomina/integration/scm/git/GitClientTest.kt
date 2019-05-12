package org.neo.gomina.integration.scm.git

import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.junit.Test

class GitClientTest {

    @Test
    fun testGitLog() {
        val client = GitClient("/Users/Amokrane/Work/Code/Idea/rxjava-test/.git")
        val repo = client.repository
        client.git.log()
                //.add(repo.resolve("refs/heads/feature1"))
                //.add(repo.resolve("refs/heads/feature2"))
                /**/
                .addRange(
                        repo.resolve("dd38ce1580b0ca5a41b83534fac6716d77acafc4"),
                        repo.resolve("eda25629ba51062bc7e58b2316808778f75a81a2")
                        //repo.resolve("refs/heads/master"),
                        //repo.resolve("refs/heads/feature2")
                )
                /**/
                .call()
                .forEach {
                    println("-> ${it.parentCount} ${it.footerLines} ${it.fullMessage} ${it.tree.name}")
                }
    }

    @Test
    fun testRevWalk() {
        val client = GitClient("/Users/Amokrane/Work/Code/Idea/rxjava-test/.git")
        //val revId = this.localRepo.resolve(commitID)
        val walk = RevWalk(client.repository)
        val branch1 = client.repository.resolve("refs/heads/branch_1")
        walk.markStart(walk.parseCommit(branch1))
        val treeWalk = TreeWalk(client.repository)
        treeWalk.addTree(walk.parseTree(branch1))
        while (treeWalk.next()) {
            println("${treeWalk.pathString}")
        }

        //client.git.repository. 6969b1809758b1339c7a582d43a8e76ca7662354
    }

    @Test
    fun testTreeWalk() {
        val client = GitClient("/Users/Amokrane/Work/Code/Idea/rxjava-test/.git")
        //val revId = this.localRepo.resolve(commitID)
        val walk = RevWalk(client.repository)
        val commit = client.repository.resolve("3348b36f0eda40912d7673bee198f4e63c09120c")
        walk.markStart(walk.parseCommit(commit))
        val treeWalk = TreeWalk(client.repository)
        treeWalk.addTree(walk.parseTree(commit))
        while (treeWalk.next()) {
            println("${treeWalk.pathString}")
        }

        //client.git.repository.
    }

    @Test
    fun testLog() {
        val client = GitClient("/Users/Amokrane/Work/Code/Idea/rxjava-test/.git")
        client.getLog("refs/heads/master", "0", 100).forEach { println(it) }
        //client.getLog("useless", Branch("branch_1"), "0", 100).forEach { println(it) }
        //client.getLog("useless", Branch("branch_2"), "0", 100).forEach { println(it) }
        //Assertions.assertThat(log.size).isEqualTo(10)
    }

    @Test
    fun testBranches() {
        val client = GitClient("/Users/Amokrane/Work/Code/Idea/rxjava-test/.git")

        client.getBranches().forEach {
            println("branch: $it")
        }
    }
}