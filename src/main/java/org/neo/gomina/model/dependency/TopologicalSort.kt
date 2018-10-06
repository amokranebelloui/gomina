package org.neo.gomina.model.dependency

import org.apache.commons.lang3.BooleanUtils
import java.util.*

private data class Dep<T>(val node: String, val metadata: T?)

class TopologicalSort<M>

    @JvmOverloads constructor(nodes: List<String> = ArrayList()) {

    //private int v;   // No. of vertices
    private val adj: MutableMap<String, MutableList<Dep<M>>> // Adjacency List

    init {
        //this.v = v;
        adj = HashMap()
        for (node in nodes) {
            adj.put(node, mutableListOf())
        }
    }

    fun addEdge(v: String, w: String, metadata: M?): TopologicalSort<M> {
        val list = getOrCreate(v)
        list.add(Dep(w, metadata))
        //adj[v].add(w);
        getOrCreate(w)
        return this
    }

    private fun getOrCreate(v: String): MutableList<Dep<M>> {
        var list: MutableList<Dep<M>>? = adj[v]
        if (list == null) {
            list = LinkedList()
            adj.put(v, list)
        }
        return list
    }

    private fun topologicalSortUtil(v: String, visited: MutableMap<String, Boolean>, stack: Stack<String>) {
        // Mark the current node as visited.
        visited.put(v, true)
        var i: Dep<*>

        // Recur for all the vertices adjacent to this vertex
        val deps = adj[v]
        if (deps != null) {
            val it = deps.iterator()
            while (it.hasNext()) {
                i = it.next()
                if (BooleanUtils.isNotTrue(visited[i.node])) {
                    topologicalSortUtil(i.node, visited, stack)
                }
            }
        }

        // Push current vertex to stack which stores result
        stack.push(v)
    }

    // The function to do Topological Sort. It uses recursive topologicalSortUtil()
    fun sort(): List<String> {
        val stack = Stack<String>()

        // Mark all the vertices as not visited
        val visited = HashMap<String, Boolean>()
        for (s in adj.keys) {
            visited.put(s, false)
        }

        // Call the recursive helper function to store Topological Sort starting from all vertices one by one
        for (s in adj.keys) {
            if (visited[s] == false) {
                topologicalSortUtil(s, visited, stack)
            }
        }

        // Print contents of stack
        val res = ArrayList<String>()
        while (stack.empty() == false) {
            res.add(stack.pop())
        }
        if (res.remove("?")) res.add("?")
        return res
    }

}

