package org.neo.gomina.persistence.model

import redis.clients.jedis.Jedis

fun String?.toList() = this?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
fun Collection<String>.toStr() = this.joinToString(separator = ",")

fun Jedis.persist(key: String, values: Map<String, String?>) {
    val updates = values.mapNotNull { it.value?.let { value -> it.key to value } }.toMap()
    val deletes = values.filterValues { it == null }.keys

    if (updates.isNotEmpty()) this.hmset(key, updates)
    if (deletes.isNotEmpty()) this.hdel(key, *deletes.toTypedArray())
}
