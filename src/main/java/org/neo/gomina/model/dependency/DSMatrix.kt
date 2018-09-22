package org.neo.gomina.model.dependency

object DSM {
    fun displayMatrix(sortedTopologically: List<String>, dependencies: Map<Link, Any>, pad:Int = 50) {
        sortedTopologically.forEachIndexed { xi, x ->
            print(x.padEnd(pad, '-') + " ")
            sortedTopologically.forEachIndexed { yi, y ->
                print(when {
                    dependencies.containsKey(Link(y, x)) -> if (x == y) "I" else if (yi > xi) "?" else "X"
                    else -> if (x == y) "-" else "."
                } + " ")
            }
            println("")
        }
    }
}
