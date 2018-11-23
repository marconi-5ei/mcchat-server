package mcchat.server

import mcchat.server.packets.*
import java.net.Socket

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
        outputStream.write(serialize(InfoPacket(0)))

        while (connection.isConnected) {
            try {
                val incoming = parser.next()

                when (incoming) {
                    is TopicListRequestPacket ->
                        outputStream.write(serialize(TopicListPacket(subscriptions.keys.toTypedArray())))

                    is SubscriptionPacket ->
                        subscriptions.subscribe(this, incoming.topic)

                    is UnsubscriptionPacket ->
                        subscriptions.unsubscribe(this, incoming.topic)

                    is MessagePacket ->
                        subscriptions[incoming.topic]?.forEach {
                            it.outputStream.write(serialize(incoming))
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        subscriptions.keys.forEach { subscriptions.unsubscribe(this, it) }

        clean()
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
