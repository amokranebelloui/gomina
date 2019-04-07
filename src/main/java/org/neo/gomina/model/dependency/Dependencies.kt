package org.neo.gomina.model.dependency

import java.util.*

data class Function(var name: String, var type: String, var sources: List<String> = emptyList()) {
    override fun toString() = "F('$name' '$type') $sources"
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Function)
            return false
        return name == other.name && type == other.type
    }
    override fun hashCode(): Int {
        return Objects.hash(name, type)
    }
}

data class Usage(var usage:Any) {
    override fun toString() = "Usage($usage)"
}

data class FunctionUsage(val function: Function, val usage: Usage? = null, var sources: List<String> = emptyList()) {
    constructor(name: String, type: String, usage: Usage? = null, sources: List<String> = emptyList()): this(Function(name, type), usage, sources)
    override fun toString() = "FUsage(${function.name} ${function.type}${usage?.let {" ${usage.usage}"} ?: ""}) $sources"
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FunctionUsage)
            return false
        return function == other.function && usage == other.usage
    }
    override fun hashCode(): Int {
        return Objects.hash(function, usage)
    }
}

data class Interactions(
        var serviceId: String, var exposed: List<Function> = emptyList(), var used: List<FunctionUsage> = emptyList()) {

    fun filterFunctionTypes(functionTypes: List<String>) = Interactions(
            serviceId = this.serviceId,
            exposed = this.exposed.filter { functionTypes.isEmpty() || functionTypes.contains(it.type) },
            used = this.used.filter { functionTypes.isEmpty() || functionTypes.contains(it.function.type) }
    )
}

fun Collection<Interactions>.merge(): Collection<Interactions> {
    val exposed = this.groupBy({ it.serviceId }) { it.exposed }.mapValues { (p, exposed) -> exposed.flatMap { it }.mergeFunctions() }
    val used = this.groupBy({ it.serviceId }) { it.used }.mapValues { (p, used) -> used.flatMap { it }.mergeFunctionUsage() }
    return (used.keys + exposed.keys).map {
        Interactions(serviceId = it, exposed = exposed[it] ?: emptyList(), used = used[it] ?: emptyList())
    }
}

fun List<Function>.mergeFunctions(): List<Function> {
    return this.groupBy({ it }) { it.sources }.map { (f, sources) -> Function(f.name, f.type, sources.flatMap { it }) }
}

fun List<FunctionUsage>.mergeFunctionUsage(): List<FunctionUsage> {
    return this.groupBy({ it }) { it.sources }.map { (f, sources) -> FunctionUsage(f.function.name, f.function.type, f.usage, sources.flatMap { it }) }
}

data class Link(val from: String, val to: String) {
    val isInternal: Boolean get() = from == to
    val isExternal: Boolean get() = from != to
    override fun toString() = "'$from' -> '$to'"
}

data class Dependency(var from: String, var to: String, var functions: List<FunctionUsage>) {
    val isInternal: Boolean get() = from == to
    val isExternal: Boolean get() = from != to
    fun involves(serviceId: String) = from == serviceId || to == serviceId
    fun involves(services: List<String>) = services.contains(from) || services.contains(to)
    fun self(serviceId: String) = from == serviceId && to == serviceId
    override fun toString() = "'$from' -> '$to'"
}

fun <T:Collection<Dependency>> T.invert(): T {
    return this.map { Dependency(it.to, it.from, it.functions) } as T
}

data class Stakeholder(val serviceId: String, val usage: Usage? = null, val sources: List<String> = emptyList())

data class Stakeholders(var users: MutableSet<Stakeholder> = mutableSetOf(), var exposers: MutableSet<String> = mutableSetOf()) {
    // FIXME Is it really used?
    fun getLinks(): List<Link> {
        return users.flatMap { user -> exposers.map { exposer -> Link(user.serviceId, exposer) } }
    }
    val exposed: Boolean get() = exposers.size > 0
    val exposedByMany: Boolean get() = exposers.size > 1
    val usageExists: Boolean get() = users.size >= 1
}

data class CallChain(val serviceId: String, val recursive: Boolean, val functions: List<FunctionUsage> = emptyList(), val calls: List<CallChain> = emptyList())

data class Counts(val incoming: Int, val self: Int, val outgoing: Int)

object Dependencies {

    fun functions(deps: Collection<Interactions>): Map<Function, Stakeholders> {
        val result = mutableMapOf<Function, Stakeholders>()
        deps.forEach { dep ->
            dep.exposed.forEach { f ->
                val stakeholders = result.getOrPut(f) { Stakeholders() }
                stakeholders.exposers.add(dep.serviceId)
            }
            dep.used.forEach { fUsage ->
                val stakeholders = result.getOrPut(fUsage.function) { Stakeholders() }
                stakeholders.users.add(Stakeholder(dep.serviceId, fUsage.usage, fUsage.sources))
            }
        }
        result.forEach { (f, stakeholders) ->
            if (stakeholders.usageExists && !stakeholders.exposed) {
                stakeholders.exposers.add("?")
            }
        }
        return result
    }

    fun interactions(functions: Map<Function, Stakeholders>): Collection<Interactions> {
        val exposed: Map<String, List<Function>> = functions
                .flatMap { (f, stakeholders) -> stakeholders.exposers.map { Pair(it, f) } }
                .groupBy( { (p,_) -> p }) { (_,v) -> v }
        val used: Map<String, List<FunctionUsage>> = functions
                .flatMap { (f, stakeholders) -> stakeholders.users.map { Pair(it.serviceId, FunctionUsage(f, it.usage, it.sources)) } }
                .groupBy( { (p,_) -> p }) { (_,v) -> v }
        return (exposed.keys + used.keys).map {
            Interactions(serviceId = it, exposed = exposed[it] ?: emptyList(), used = used[it] ?: emptyList())
        }
    }

    fun dependencies(functions: Map<Function, Stakeholders>): List<Dependency> {
        return functions
                .map { (function, stakeholders) -> Pair(function, stakeholders) }
                .flatMap { (function, stakeholders) -> stakeholders.users.map { Triple(function, it, stakeholders.exposers) } }
                .flatMap { (function, user, exposers) -> exposers.map { Triple(FunctionUsage(function, user.usage, user.sources), user, it) } }
                .groupBy { (fUsage, user, exposer ) -> Link(user.serviceId, exposer) }
                .map { (link, group) -> Dependency(link.from, link.to, group.map { it.first}) }
    }

    private fun buildCallChain(serviceId: String, dependencies: List<Dependency>, parentFunctions: List<FunctionUsage> = emptyList(), seen: List<String> = arrayListOf("?")/*, max: Int = 0*/): CallChain {
        val cleanDeps = dependencies
                .map {
                    Dependency(
                        it.from, it.to,
                        it.functions.filter { f -> f.function.type != "db-interaction" && f.function.type != "db-multi" && f.function.type != "db-read"  } // FIXME Configurable
                    )
                }
                .filter { it.functions.isNotEmpty() }
        val self = cleanDeps.find { it.self(serviceId) }?.functions?: emptyList()
        val related = if (!seen.contains(serviceId)/* && max >= 0*/) {
            cleanDeps
                .filter { it.from == serviceId && !seen.contains(it.to) }
                .map {
                    when {
                        it.self(serviceId) -> CallChain(it.to, false, self)
                        else -> buildCallChain(it.to, cleanDeps, it.functions, seen + serviceId/*, max + 1*/)
                    }
                }
        }
        else emptyList()
        return CallChain(serviceId, false, parentFunctions, related)
    }

    fun invocationChain(serviceId: String, dependencies: List<Dependency>): CallChain {
        return buildCallChain(serviceId, dependencies, seen = mutableListOf())
    }

    fun callChain(serviceId: String, dependencies: List<Dependency>): CallChain {
        return buildCallChain(serviceId, dependencies.invert(), seen = mutableListOf())
    }

    fun infer(source: String, users: Set<Stakeholder>, from: Any?, to: Any?, usageSelector: (Usage?) -> Any?): Stakeholders {
        return users
                .groupBy( { usageSelector(it.usage)} ) { it.serviceId }
                .let { map ->
                    Stakeholders(
                            users = map[from]?.map { Stakeholder(it, Usage("$from->$to"), listOf(source)) }?.toMutableSet() ?: mutableSetOf(),
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
                .map { (node, types) ->
                    val counts = types.groupBy { it }.let { Counts(it.count("IN"), it.count("SELF"), it.count("OUT")) }
                    Pair(node, counts)
                }
                .toMap()
    }

}

interface EnrichDependencies {
    fun enrich(interactions: Collection<Interactions>): Collection<Interactions>
}
