package org.neo.gomina.model.host

import com.google.inject.name.Named
import java.net.InetAddress
import javax.inject.Inject

fun main(args: Array<String>) {
    val hosts = HostUtils()
    hosts.domains = arrayListOf("ad.exane.com")
    println(hosts.resolve("10.132.64.3"))
    println(hosts.resolve("90.132.64.3"))
    println(hosts.resolve("google.com"))
    println(hosts.resolve("10.143.29.42"))
    println(hosts.resolve(null))
}

class HostUtils {

    @Inject @Named("domains") lateinit var domains: List<String>

    fun resolve(ip: String?): String? {
        if (ip != null) {
            var hostname = ip
            try {
                val host = InetAddress.getByName(ip)
                if (host?.hostName?.isNotBlank() == true) {
                    hostname = simple(host.hostName, domains)
                }
            }
            catch (e: Exception) {
                println("Unknown address $ip")
            }
            return hostname
        }
        return null
    }
}

// FIXME this can be applied only for internal addresses
private fun simple(hostName: String, domains: List<String>): String {
    //return if (hostName.contains('.')) hostName.substring(0, hostName.indexOf('.')) else hostName
    var result = hostName
    domains.forEach {
        if (result.endsWith(".$it")) {
            result = result.dropLast(it.length + 1)
        }
    }
    return result
}
