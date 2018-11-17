package mcchat.server.packets

import mcchat.server.helpers.getFrom
import mcchat.server.helpers.nullTerminate
import mcchat.server.helpers.write
import java.io.ByteArrayOutputStream

sealed class Packet {
    abstract val opcode: Byte

    fun bytes(): ByteArray {
        val out = ByteArrayOutputStream()

        for (field in this.javaClass.declaredFields)
            when (field.type) {
                Byte::class.java -> out.write(field.getFrom(this) as Byte)

                String::class.java -> out.write(
                    nullTerminate((field.getFrom(this) as String).toByteArray())
                )

                Array<String>::class.java -> out.write(
                    nullTerminate(
                        (field.getFrom(this) as Array<String>)
                            .map { it.toByteArray() }
                            .map(::nullTerminate)
                            .reduce(ByteArray::plus)
                    )
                )
            }

        return out.toByteArray()
    }
}

class InfoPacket(internal val version: Byte) : Packet() {
    override val opcode = (0).toByte()
}

class TopicListRequestPacket : Packet() {
    override val opcode = (4).toByte()
}

class TopicListPacket(internal val topics: Array<String>) : Packet() {
    override val opcode = (5).toByte()
}

sealed class TopicPacket : Packet() {
    abstract val topic: String
}

class SubscriptionPacket(override val topic: String) : TopicPacket() {
    override val opcode = (1).toByte()
}

class UnsubscriptionPacket(override val topic: String) : TopicPacket() {
    override val opcode = (2).toByte()
}

class MessagePacket(
    override val topic: String,
    internal val username: String,
    internal val message: String
) : TopicPacket() {
    override val opcode = (3).toByte()
}
