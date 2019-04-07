package org.neo.gomina.model.dependency

import org.fest.assertions.Assertions.*
import org.junit.Test

class DependenciesTest {

    @Test
    fun testProjectDependencies() {
        println("# Project Dependencies (order)")
        println(order.serviceId)
        println("Exposed ${order.exposed}")
        println("Used ${order.used}")
    }

    @Test
    fun testDependencies() {
        println("# Functions")
        val functions = Dependencies.functions(components)
        functions.forEach { f, stakeholders -> println("$f => $stakeholders") }

        println("# Links")
        functions.map { (f, stakeholders) -> Pair(f, stakeholders.getLinks()) }
                .forEach { (f, links) -> println("$f $links") }

        println("# Dependencies")
        val dependencies = Dependencies.dependencies(functions)
        dependencies.forEach { println("$it")}
    }

    @Test
    fun testMerge() {
        val p1 = Interactions(serviceId = "p1",
                exposed = listOf(
                        Function("f1", "command", sources = listOf("auto"))
                ),
                used = listOf(
                        FunctionUsage("f2", "request", sources = listOf("auto")),
                        FunctionUsage("f3", "database", Usage(DbMode.WRITE), sources = listOf("auto")),
                        FunctionUsage("f5", "request", sources = listOf("auto"))
                )
        )
        val p1ext = Interactions(serviceId = "p1",
                used = listOf(
                        FunctionUsage("f5", "request", sources = listOf("ext"))
                )
        )
        val p2 = Interactions(serviceId = "p2",
                exposed = listOf(
                        Function("f2", "request", sources = listOf("auto")),
                        Function("f4", "request", sources = listOf("auto"))
                )
        )
        val p2ext  = Interactions(serviceId = "p2",
                exposed = listOf(
                        Function("f2", "request", sources = listOf("ext")),
                        Function("f5", "request", sources = listOf("ext"))
                )
        )
        val merge = listOf(p1, p1ext, p2, p2ext).merge().toList()

        merge.forEach { println(it) }
        assertThat(merge.size).isEqualTo(2)
        assertThat(merge[0].used).hasSize(3)
        assertThat(merge[0].exposed).hasSize(1)
        assertThat(merge[1].used).hasSize(0)
        assertThat(merge[1].exposed).hasSize(3)
    }

    @Test
    fun testInvocationCallChain() {
        val functions = Dependencies.functions(components)
        val dependencies = Dependencies.dependencies(functions)

        dependencies.forEach { println(it) }
        fun printNode(cc:CallChain, padding: String = "") {
            println("$padding${cc.serviceId}${if (cc.recursive) " @" else ""} ${cc.functions}")
            cc.calls.forEach { printNode(it, "$padding ") }
        }

        println("# Invocation Chain")
        printNode(Dependencies.invocationChain("basket", dependencies))

        println("# Call Chain")
        printNode(Dependencies.callChain("referential", dependencies))
    }

    @Test
    fun testSpecialUsageFunctions() { // Transitive implicit dependencies
        println("# Special Dependencies")
        val specialFunctions = components
                .map { p ->
                    Interactions(serviceId = p.serviceId,
                            exposed = p.exposed,
                            used = p.used.filter { it.function.type == "database" })
                }
                .let { Dependencies.functions(it) }
                .filter { (f, stakeholders) -> stakeholders.usageExists }
                .mapValues { (f, stakeholders) ->
                    Dependencies.infer("inferred", stakeholders.users, DbMode.READ, DbMode.WRITE) { it?.usage }
                }
        specialFunctions.forEach { println("$it")}

        println("# Special Interactions")
        Dependencies.interactions(specialFunctions).forEach { println("$it") }
    }

    @Test
    fun testForwardDependencies() {
        val functions = Dependencies.functions(components)
        val dependencies = Dependencies.dependencies(functions)
        val outgoing = dependencies.groupBy { it.from }

        println("# Forward Dependencies")
        components.map { it.serviceId }.forEach { component ->
            println("$component")
            outgoing[component]?.apply { forEach { println("   ${it.to} : #${it.functions.size} ${it.functions}") } }
        }
    }

    @Test
    fun testReverseDependencies() {
        val functions = Dependencies.functions(components)
        val dependencies = Dependencies.dependencies(functions)
        val incoming = dependencies.groupBy { it.to }

        println("# Reverse Dependencies")
        components.map { it.serviceId }.forEach { component ->
            println("$component")
            incoming[component]?.apply { forEach { println("   ${it.from} : #${it.functions.size} ${it.functions}") } }
        }
    }

    @Test
    fun testIncomingOutgoingCounts() {
        val functions = Dependencies.functions(components)
        val dependencies = Dependencies.dependencies(functions)

        println("# Incoming/Outgoing")
        val counts = Dependencies.counts(dependencies)
        counts.forEach { (node, counts) -> println("$node $counts") }
    }

    @Test
    fun testDSM() {
        val functions = Dependencies.functions(components)
        val dependencies = Dependencies.dependencies(functions)

        println("# Dependency Matrix")
        val g = TopologicalSort<Dependency>(components.map { it.serviceId }).also {
                    dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
                }

        DSM.displayMatrix(g.sort(), dependencies.groupBy { Link(it.from, it.to) })
    }

    @Test
    fun testDoubleResponsibilities() {
        val functions = Dependencies.functions(components)

        println("# Double Responsibilities")
        val doubleResponsibilities = functions.filterValues { it.exposedByMany }
        doubleResponsibilities.forEach { f, stakeholders -> println("$f ${stakeholders.exposers}") }

        println("# Collapsing Responsibilities")
        val key = { components: Collection<String> -> components.sorted().joinToString(separator = ",") }
        val collapsingResponsibilities = doubleResponsibilities
                .map { (f, stakeholders) -> Pair(key(stakeholders.exposers.toSet()), f) }
                .groupBy({ (exposers, f) -> exposers }) { (_, f) -> f }
        collapsingResponsibilities.forEach { (exposers, f) -> println("$exposers $f") }
    }
}