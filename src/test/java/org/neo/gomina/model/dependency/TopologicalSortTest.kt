package org.neo.gomina.model.dependency

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

class TopologicalSortTest {

    @Test
    fun testSort() {
        val res = TopologicalSort<String>()
                .addEdge("5", "2", "authentication")
                .addEdge("5", "0", null)
                .addEdge("4", "0", null)
                .addEdge("4", "1", "get data")
                .addEdge("2", "3", null)
                .addEdge("3", "1", null)
                .sort()

        println("Following is a Topological sort of the given graph")
        println(res)
        assertThat(res).containsExactly("5", "4", "2", "3", "1", "0")
    }
}