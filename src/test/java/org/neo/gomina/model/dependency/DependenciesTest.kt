package org.neo.gomina.model.dependency

import org.fest.assertions.Assertions.*
import org.junit.Test

class DependenciesTest {

    @Test
    fun testProjectDependencies() {
        println("# Project Dependencies (order)")
        println(order.projectId)
        println("Exposed ${order.exposed}")
        println("Used ${order.used}")
    }

    @Test
    fun testDependencies() {
        println("# Functions")
        val functions = Dependencies.functions(projects)
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
        val p1 = Interactions(projectId = "p1",
                exposed = listOf(
                        Function("f1", "command")
                ),
                used = listOf(
                        FunctionUsage("f2", "request"),
                        FunctionUsage("f3", "database", Usage(DbMode.WRITE))
                )
        )
        val p1ext = Interactions(projectId = "p1",
                used = listOf(
                        FunctionUsage("f5", "request")
                )
        )
        val p2 = Interactions(projectId = "p2",
                exposed = listOf(
                        Function("f2", "request"),
                        Function("f4", "request")
                )
        )
        val p2ext  = Interactions(projectId = "p2",
                exposed = listOf(
                        Function("f5", "request")
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
    fun testSpecialUsageFunctions() { // Transitive implicit dependencies
        println("# Special Dependencies")
        val specialFunctions = projects
                .map { p ->
                    Interactions(projectId = p.projectId,
                            exposed = p.exposed,
                            used = p.used.filter { it.function.type == "database" })
                }
                .let { Dependencies.functions(it) }
                .filter { (f, stakeholders) -> stakeholders.usageExists }
                .mapValues { (f, stakeholders) ->
                    Dependencies.infer(stakeholders.users, DbMode.READ, DbMode.WRITE) { it?.usage }
                }
        specialFunctions.forEach { println("$it")}

        println("# Special Interactions")
        Dependencies.interactions(specialFunctions).forEach { println("$it") }
    }

    @Test
    fun testForwardDependencies() {
        val functions = Dependencies.functions(projects)
        val dependencies = Dependencies.dependencies(functions)
        val outgoing = dependencies.groupBy { it.from }

        println("# Forward Dependencies")
        projects.map { it.projectId }.forEach { project ->
            println("$project")
            outgoing[project]?.apply { forEach { println("   ${it.to} : #${it.functions.size} ${it.functions}") } }
        }
    }

    @Test
    fun testReverseDependencies() {
        val functions = Dependencies.functions(projects)
        val dependencies = Dependencies.dependencies(functions)
        val incoming = dependencies.groupBy { it.to }

        println("# Reverse Dependencies")
        projects.map { it.projectId }.forEach { project ->
            println("$project")
            incoming[project]?.apply { forEach { println("   ${it.from} : #${it.functions.size} ${it.functions}") } }
        }
    }

    @Test
    fun testIncomingOutgoingCounts() {
        val functions = Dependencies.functions(projects)
        val dependencies = Dependencies.dependencies(functions)

        println("# Incoming/Outgoing")
        val counts = Dependencies.counts(dependencies)
        counts.forEach { (project, counts) -> println("$project $counts") }
    }

    @Test
    fun testDSM() {
        val functions = Dependencies.functions(projects)
        val dependencies = Dependencies.dependencies(functions)

        println("# Dependency Matrix")
        val g = TopologicalSort<Dependency>(projects.map { it.projectId }).also {
                    dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
                }

        DSM.displayMatrix(g.sort(), dependencies.groupBy { Link(it.from, it.to) })
    }

    @Test
    fun testDoubleResponsibilities() {
        val functions = Dependencies.functions(projects)

        println("# Double Responsibilities")
        val doubleResponsibilities = functions.filterValues { it.exposedByMany }
        doubleResponsibilities.forEach { f, stakeholders -> println("$f ${stakeholders.exposers}") }

        println("# Collapsing Responsibilities")
        val key = { projects: Collection<String> -> projects.sorted().joinToString(separator = ",") }
        val collapsingResponsibilities = doubleResponsibilities
                .map { (f, stakeholders) -> Pair(key(stakeholders.exposers.toSet()), f) }
                .groupBy({ (exposers, f) -> exposers }) { (_, f) -> f }
        collapsingResponsibilities.forEach { (exposers, f) -> println("$exposers $f") }
    }
}