package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.neo.gomina.model.instances.Instance
import org.neo.gomina.model.monitoring.Indicators

fun Instance.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["pid"]
    this.host = indicators["host"]
    this.version = indicators["version"]
    this.revision = indicators["revision"]

    applyCluster(indicators)

    this.status = indicators["status"]
    this.jmx = indicators["jmx"]?.toInt()
    this.busVersion = indicators["busVersion"]
    this.coreVersion = indicators["coreVersion"]
    this.quickfixPersistence = indicators["quickfixPersistence"]

    applyRedis(indicators)

    if (StringUtils.isNotBlank(this.deployHost) && this.deployHost != this.host) {
        this.unexpectedHost = true
    }
}

private fun Instance.applyCluster(indicators: Indicators) {
    this.cluster = indicators["cluster"]?.toBoolean() ?: false
    this.participating = indicators["participating"]?.toBoolean() ?: false
    this.leader = indicators["leader"]?.toBoolean() ?: true // Historically we didn't have this field
}

private fun Instance.applyRedis(indicators: Indicators) {
    this.redisHost = indicators["redisHost"]
    this.redisPort = indicators["redisPort"]?.toInt()
    this.redisMasterHost = indicators["redisMasterHost"]
    this.redisMasterPort = indicators["redisMasterPort"]?.toInt()
    this.redisMasterLink = indicators["redisMasterLink"]?.toBoolean()
    this.redisMasterLinkDownSince = indicators["redisMasterLinkDownSince"]
    this.redisOffset = indicators["redisOffset"]?.toInt()
    this.redisOffsetDiff = indicators["redisOffsetDiff"]?.toInt()
    this.redisMaster = indicators["redisMaster"]?.toBoolean()
    this.redisRole = indicators["redisRole"]
    this.redisRW = indicators["redisRW"]
    this.redisMode = indicators["redisMode"]
    this.redisStatus = indicators["redisStatus"]
    this.redisSlaveCount = indicators["redisSlaveCount"]?.toInt()
    this.redisClientCount = indicators["redisClientCount"]?.toInt()
}

// FIXME Easier to have it on the UI level
private fun isLive(indicators: Map<String, Any>): Boolean {
    val timestamp = indicators["timestamp"] as LocalDateTime?  // FIXME Date format
    return if (timestamp != null) LocalDateTime(DateTimeZone.UTC).minusSeconds(1).isAfter(timestamp) else true
}
