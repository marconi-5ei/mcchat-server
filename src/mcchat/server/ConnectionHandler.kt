package mcchat.server

import mcchat.server.helpers.stripSlash
import mcchat.server.packets.*
import mcchat.server.packets.serialization.Parser
import mcchat.server.packets.serialization.serialize
import java.io.IOException
import java.net.Socket

// TODO Add logging

class ConnectionHandler(private val connection: Socket) : Runnable {
    private val inputStream = connection.getInputStream()
    private val outputStream = connection.getOutputStream()

    private val parser = Parser(inputStream)

    // TODO check if termination is done correctly (flush, etc.)
    private fun clean() {
        inputStream.close()
        outputStream.close()
        connection.close()
    }

    override fun run() {

        println("INFO: " + connection.inetAddress.stripSlash() + ":" + connection.port + " connected")

        outputStream.write(serialize(InfoPacket(0)))
        println("INFO: InfoPakcet sent to " + connection.inetAddress.stripSlash() + ":" + connection.port)

        while (connection.isConnected) {
            val incoming = try {
                parser.next()
            } catch (e: IOException) {
                break
            }

            when (incoming) {
                is TopicListRequestPacket -> {
                    println("VERBOSE: TopicListRequestPacket received from " + connection.inetAddress.stripSlash() + ":" + connection.port)
                    outputStream.write(serialize(TopicListPacket(subscriptions.keys.toTypedArray())))
                    println("VERBOSE: TopicListPakcet sent to " + connection.inetAddress.stripSlash() + ":" + connection.port)
                }

                is SubscriptionPacket -> {
                    println("VERBOSE: SubscriptionPacket received from " + connection.inetAddress.stripSlash() + ":" + connection.port)
                    subscriptions.subscribe(this, incoming.topic)
                    println("VERBOSE: " + connection.inetAddress.stripSlash() + ":" + connection.port + " subscribed to " + incoming.topic)
                }

                is UnsubscriptionPacket -> {
                    println("VERBOSE: UnsubscriptionPacket received from " + connection.inetAddress.stripSlash() + ":" + connection.port)
                    subscriptions.unsubscribe(this, incoming.topic)
                    println("VERBOSE: " + connection.inetAddress.stripSlash() + ":" + connection.port + " unsubscribed from " + incoming.topic)
                }

                is MessagePacket -> {
                    println("VERBOSE: MessagePacket received from " + connection.inetAddress.stripSlash() + ":" + connection.port)
                    subscriptions[incoming.topic]?.forEach {
                        it.outputStream.write(serialize(incoming))
                    }
                }
            }
        }

        subscriptions.keys.forEach { subscriptions.unsubscribe(this, it) }

        clean()

        println("INFO: " + connection.inetAddress.stripSlash() + ":" + connection.port + " disconnected")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionHandler

        if (connection.inetAddress != other.connection.inetAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return connection.inetAddress.hashCode()
    }
}
