package mcchat.server

import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

internal val subscriptions: MutableMap<String, MutableSet<ConnectionHandler>> = ConcurrentHashMap()

fun MutableMap<String, MutableSet<ConnectionHandler>>.subscribe(client: ConnectionHandler, topic: String) {
    synchronized(this) {
        putIfAbsent(topic, ConcurrentHashMap.newKeySet())

        this[topic]!!.add(client)
    }
}

fun MutableMap<String, MutableSet<ConnectionHandler>>.unsubscribe(client: ConnectionHandler, topic: String) {
    synchronized(this) {
        if (this[topic] != null) {
            this[topic]!!.remove(client)

            if (this[topic]!!.isEmpty())
                this.remove(topic)
        }
    }
}

fun main(args: Array<String>) {
    println("INFO: Server started. Version 0.1")

    ServerSocket(1502).use { watchdog ->
        while (true) {
            thread(isDaemon = true, block = ConnectionHandler(watchdog.accept())::run)
        }
    }
}
