package org.neo.gomina.model.dependency

data class Function(var name: String, var type: String)

data class Usage(var usage:Any)

data class FunctionUsage(val function: Function, val usage: Usage? = null) {
    constructor(name: String, type: String, usage: Usage? = null): this(Function(name, type), usage)
}

data class ProjectDeps (
        var projectId: String,
        var exposed: List<Function> = emptyList(),
        var used: List<FunctionUsage> = emptyList()
)

data class Link(val from: String, val to: String) {
    val isInternal: Boolean get() = from == to
    val isExternal: Boolean get() = from != to
    override fun toString() = "'$from' -> '$to'"
}

data class Dependency(var from: String, var to: String, var functions: List<FunctionUsage>) {
    val isInternal: Boolean get() = from == to
    val isExternal: Boolean get() = from != to
    override fun toString() = "'$from' -> '$to'"
}

data class Stakeholder(val projectId: String, val usage: Usage? = null)

data class Stakeholders(var users: MutableSet<Stakeholder> = mutableSetOf(), var exposers: MutableSet<String> = mutableSetOf()) {
    fun getLinks(): List<Link> {
        return users.flatMap { user -> exposers.map { exposer -> Link(user.projectId, exposer) } }
    }
    val exposed: Boolean get() = exposers.size > 0
    val exposedByMany: Boolean get() = exposers.size > 1
    val usageExists: Boolean get() = users.size >= 1
}

data class Counts(val incoming: Int, val self: Int, val outgoing: Int)

object Dependencies {

    fun functions(deps: List<ProjectDeps>): Map<Function, Stakeholders> {
        val result = mutableMapOf<Function, Stakeholders>()
        deps.forEach { dep ->
            dep.exposed.forEach { f ->
                val stakeholders = result.getOrPut(f) { Stakeholders() }
                stakeholders.exposers.add(dep.projectId)
            }
            dep.used.forEach { fUsage ->
                val stakeholders = result.getOrPut(fUsage.function) { Stakeholders() }
                stakeholders.users.add(Stakeholder(dep.projectId, fUsage.usage))
            }
        }
        result.forEach { (f, stakeholders) ->
            if (stakeholders.usageExists && !stakeholders.exposed) {
                stakeholders.exposers.add("?")
            }
        }
        return result
    }

    fun dependencies(functions: Map<Function, Stakeholders>): List<Dependency> {
        return functions
                .map { (function, stakeholders) -> Pair(function, stakeholders) }
                .flatMap { (function, stakeholders) -> stakeholders.users.map { Triple(function, it, stakeholders.exposers) } }
                .flatMap { (function, user, exposers) -> exposers.map { Triple(FunctionUsage(function, user.usage), user, it) } }
                .groupBy { (fUsage, user, exposer ) -> Link(user.projectId, exposer) }
                .map { (link, group) -> Dependency(link.from, link.to, group.map { it.first}) }
    }

    fun infer(users: Set<Stakeholder>, from: Any?, to: Any?, usageSelector: (Usage?) -> Any?): Stakeholders {
        return users
                .groupBy( { usageSelector(it.usage)} ) { it.projectId }
                .let { map ->
                    Stakeholders(
                            users = map[from]?.map { Stakeholder(it, Usage("$from->$to")) }?.toMutableSet() ?: mutableSetOf(),
                            exposers = map[to]?.toMutableSet() ?: mutableSetOf()
                    )
                }
    }

    fun counts(dependencies: List<Dependency>): Map<String, Counts> {
        fun Map<String, List<String>>.count(key: String) = this[key]?.size ?: 0
        return dependencies
                .flatMap {
                    if (it.isInternal) listOf(Triple(it.from, it.to, "SELF"))
                    else listOf(Triple(it.from, it.to, "OUT"), Triple(it.to, it.from, "IN"))
                }
                .groupBy({ (from, to, type) -> from }) { (_, _, type) -> type }
                .map { (project, types) ->
                    val counts = types.groupBy { it }.let { Counts(it.count("IN"), it.count("SELF"), it.count("OUT")) }
                    Pair(project, counts)
                }
                .toMap()
    }

}

