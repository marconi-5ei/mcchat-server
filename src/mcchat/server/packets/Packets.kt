@file:Suppress("unused")

package mcchat.server.packets

open class OpCoded(val opcode: Byte)

sealed class Packet

class InfoPacket(internal val version: Byte) : Packet() {
    companion object : OpCoded(0)
}

class TopicListRequestPacket : Packet() {
    companion object : OpCoded(4)
}

class TopicListPacket(internal val topics: Array<String>) : Packet() {
    companion object : OpCoded(5)
}

sealed class TopicPacket(val topic: String) : Packet()

class SubscriptionPacket(topic: String) : TopicPacket(topic) {
    companion object : OpCoded(1)
}

class UnsubscriptionPacket(topic: String) : TopicPacket(topic) {
    companion object : OpCoded(2)
}

class MessagePacket(
    topic: String,
    internal val username: String,
    internal val message: String
) : TopicPacket(topic) {
    companion object : OpCoded(3)
}
