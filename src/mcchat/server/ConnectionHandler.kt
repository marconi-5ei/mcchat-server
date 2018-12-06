package mcchat.server

import mcchat.server.packets.*
import mcchat.server.packets.serialization.Parser
import mcchat.server.packets.serialization.serialize
import java.io.IOException
import java.net.Socket

class ConnectionHandler(private val connection: Socket) : Runnable {
    private val identifier = "${connection.inetAddress.toString().replace("/", "")}: ${connection.port}"

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

        println("INFO: $identifier connected")

        outputStream.write(serialize(InfoPacket(0)))
        println("INFO: InfoPakcet sent to $identifier")

        while (connection.isConnected) {
            val incoming = try {
                parser.next()
            } catch (e: IOException) {
                break
            }

            println("VERBOSE: ${incoming::class.simpleName} received from $identifier")

            when (incoming) {
                is TopicListRequestPacket -> {
                    outputStream.write(serialize(TopicListPacket(subscriptions.keys.toTypedArray())))
                    println("VERBOSE: TopicListPakcet sent to $identifier")
                }

                is SubscriptionPacket -> {
                    subscriptions.subscribe(this, incoming.topic)
                    println("VERBOSE: $identifier subscribed to ${incoming.topic}")
                }

                is UnsubscriptionPacket -> {
                    subscriptions.unsubscribe(this, incoming.topic)
                    println("VERBOSE: $identifier unsubscribed from ${incoming.topic}")
                }

                is MessagePacket ->
                    subscriptions[incoming.topic]?.forEach {
                        it.outputStream.write(serialize(incoming))
                        println("VERBOSE: MessagePacket sent to ${it.identifier}")
                    }
            }
        }

        subscriptions.keys.forEach { subscriptions.unsubscribe(this, it) }

        clean()

        println("INFO: $identifier disconnected")
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
