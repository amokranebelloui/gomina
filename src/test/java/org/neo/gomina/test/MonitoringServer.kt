package org.neo.gomina.test

import org.zeromq.ZMQ
import kotlin.concurrent.thread

private fun server() {
    val listenPort = 7401
    val publishPort = 7402
    val context = ZMQ.context(1)

    val receiver = context.socket(ZMQ.PULL)
    receiver.bind("tcp://*:$listenPort")

    val publisher = context.socket(ZMQ.PUB)
    publisher.bind("tcp://*:$publishPort")

    println("Listening to $listenPort and publishing to $publishPort")
    thread(start = true) {
        while (!Thread.currentThread().isInterrupted) {
            val obj = receiver.recvStr(0)
            println("Received $obj")
            publisher.send(obj)
        }
        println("closed")
    }

    Thread.currentThread().join()
    receiver.close()
    context.term()
}

fun main(args: Array<String>) {
    server()
}

