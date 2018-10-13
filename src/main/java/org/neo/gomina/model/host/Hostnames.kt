package org.neo.gomina.model.host

import java.net.InetAddress

fun main(args: Array<String>) {
    println(resolveHostname("10.132.64.3"))
    println(resolveHostname("90.132.64.3"))
    println(resolveHostname("google.com"))
    println(resolveHostname("10.143.29.42"))
}

fun resolveHostname(ip: String?): String? {
    if (ip != null) {
        var hostname = ip
        try {
            val host = InetAddress.getByName(ip)
            if (host?.hostName?.isNotBlank() == true) {
                hostname = simple(host.hostName)
            }
        }
        catch (e: Exception) {
            println("Unknown address $ip")
        }
        return hostname
    }
    return null
}

// FIXME this can be applied only for internal addresses
private fun simple(hostName: String): String {
    //return if (hostName.contains('.')) hostName.substring(0, hostName.indexOf('.')) else hostName
    return hostName
}
