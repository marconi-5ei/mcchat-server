package mcchat.server

import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

typealias Subscriptions = MutableMap<String, MutableSet<ConnectionHandler>>

internal val subscriptions: Subscriptions = ConcurrentHashMap()

internal fun Subscriptions.subscribe(client: ConnectionHandler, topic: String) {
    synchronized(this) {
        putIfAbsent(topic, ConcurrentHashMap.newKeySet())

        this[topic]!!.add(client)
    }
}

internal fun Subscriptions.unsubscribe(client: ConnectionHandler, topic: String) {
    synchronized(this) {
        if (this[topic] != null) {
            this[topic]!!.remove(client)

            if (this[topic]!!.isEmpty())
                this.remove(topic)
        }
    }
}

fun main(args: Array<String>) {
    println("INFO: Server started. Version 1.1.0")

    ServerSocket(1502).use { watchdog ->
        while (true) {
            thread(isDaemon = true, block = ConnectionHandler(watchdog.accept())::run)
        }
    }
}
