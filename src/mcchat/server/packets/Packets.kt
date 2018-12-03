@file:Suppress("unused")

package mcchat.server.packets

import mcchat.server.helpers.Position

open class OpCoded(val opcode: Byte)

sealed class Packet

class InfoPacket(@Position(0) internal val version: Byte) : Packet() {
    companion object : OpCoded(0)
}

class TopicListRequestPacket : Packet() {
    companion object : OpCoded(4)
}

sealed class ListPacket : Packet() {
    companion object {
        const val TERMINATOR: Byte = 4
    }
}

class TopicListPacket(@Position(0) internal val topics: Array<String>) : ListPacket() {
    companion object : OpCoded(5)
}

sealed class TopicPacket(@Position(0) val topic: String) : Packet()

class SubscriptionPacket(topic: String) : TopicPacket(topic) {
    companion object : OpCoded(1)
}

class UnsubscriptionPacket(topic: String) : TopicPacket(topic) {
    companion object : OpCoded(2)
}

class MessagePacket(
    topic: String,
    @Position(1) internal val username: String,
    @Position(2) internal val message: String
) : TopicPacket(topic) {
    companion object : OpCoded(3)
}
