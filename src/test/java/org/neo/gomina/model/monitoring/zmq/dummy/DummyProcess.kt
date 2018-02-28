package org.neo.gomina.model.monitoring.zmq.dummy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import org.zeromq.ZMQ
import java.io.File
import kotlin.concurrent.thread

fun test(env: String) {
    val file = File("datadummy/mon.$env.yaml")
    if (file.exists()) {
        println("Starting...")
        val context = ZMQ.context(1)
        val subscriber = context.socket(ZMQ.PUSH)
        subscriber.connect("tcp://localhost:7401")
        Thread.sleep(100) // Connection to be established
        println("Started")
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            println("Closing...")
            subscriber.close()
            context.term()
            println("Closed")
        })

        val mapper = ObjectMapper(YAMLFactory())

        while (true) {
            //val list: List<Map<String, Any>> = mapper.readValue(file, object : TypeReference<List<Map<String, Any>>>(){})
            val list = mapper.readValue<List<Map<String, Any>>>(file)
            for (indicators in list) {
                val instanceId = indicators.get("name") as String
                val keyValue = indicators.asIterable().joinToString(separator = ";") {
                    "${it.key}=${it.value}"
                }
                val msg = ".#HB.$env.$instanceId.*.0;$keyValue"
                subscriber.send(msg)
                println("Sent $msg")
            }
            Thread.sleep(1000)
        }
    }
    else {
        println("File $file doesn't exist")
    }
}

fun main(args: Array<String>) {
    test("UAT")
}

