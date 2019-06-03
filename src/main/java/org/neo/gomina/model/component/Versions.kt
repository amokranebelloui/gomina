package org.neo.gomina.model.component

import com.google.inject.Inject
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology

class ComponentVersionService {

    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var inventory: Inventory
    @Inject lateinit var topology: Topology

    fun getVersions(componentId: String): List<VersionRelease> {
        return componentRepo.get(componentId)?.let { component ->
            val versions = componentRepo.getVersions(component.id, null)
            filterBranchDuplicates(component, versions).sortedByDescending { it.version }

        }
        ?: emptyList()
    }

    fun getCurrentVersions(componentId: String): List<VersionRelease> {
        return componentRepo.get(componentId)?.let { c ->
            val environments = inventory.getEnvironments()
            val instances = topology.buildExtInstances(c, environments)
            val currentVersions = currentVersions(c, instances)
            filterBranchDuplicates(c, currentVersions).sortedByDescending { it.version }
        }
        ?: emptyList()
    }

    fun currentVersions(component: Component, instances: List<ExtInstance>): List<VersionRelease> {
        val instanceSmallestVersion = instances
                .flatMap { it.versions }
                .map { it.simple() }
                .sortedBy { it }
                .firstOrNull()
        val versions = componentRepo.getVersions(component.id, null)
        return if (instanceSmallestVersion != null) {
            versions.filter { it.version >= instanceSmallestVersion }
        }
        else {
            versions.groupBy({ it.branchId }, { it })
                    .flatMap { (_, versions) ->
                        versions.sortedByDescending { it.version }.take(2)
                    }
        }
    }

    /**
     * Versions on trunk and active branches.
     * When branching from a tag the version is on both the origin and the target branch,
     * sort by date and take only the first one
     */
    private fun filterBranchDuplicates(component: Component, versions: List<VersionRelease>): List<VersionRelease> {
        val activeBranches = component.branches.filter { !it.dismissed }.map { it.name }
        val activeVersions = versions
                .filter { activeBranches.contains(it.branchId) }

        val stable = activeVersions.filter { it.version.isStable() }
                .groupBy { it.version.simple() }
                .map { (_, versions) -> versions.sortedBy { it.releaseDate }.first() }
        val snapshots = activeVersions.filter { !it.version.isStable() }
        return stable + snapshots
    }

}