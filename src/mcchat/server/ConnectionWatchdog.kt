package mcchat.server

import java.io.IOException
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
    try {
        ServerSocket(1502).use { watchdog ->
            while (true) {
                try {
                    thread(isDaemon = true, block = ConnectionHandler(watchdog.accept())::run)
                } catch (ignored: IOException) {

                }

            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
