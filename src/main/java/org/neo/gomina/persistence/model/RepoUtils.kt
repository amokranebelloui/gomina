package org.neo.gomina.persistence.model

fun String?.toList() = this?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
fun Collection<String>.toStr() = this.joinToString(separator = ",")
