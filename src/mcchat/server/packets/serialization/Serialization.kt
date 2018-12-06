@file:Suppress("unused")

package mcchat.server.packets.serialization

import mcchat.server.helpers.*
import mcchat.server.packets.Packet
import java.io.InputStream
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

// TODO Consider replacing reflection with code generation (define packets as lists of pairs (<name> to <type>::class) and ancestors to inherit from)

class Parser(private val input: InputStream) : Iterator<Packet?> {
    override fun hasNext(): Boolean {
        return input.available() > 0
    }

    override fun next(): Packet {
        val opcode = input.readByte()

        @Suppress("UNCHECKED_CAST")
        val packetClass =
            packetsByOpCode[opcode] ?: throw IllegalArgumentException("No packet with opcode \"$opcode\" is defined")

        val packetFields = packetClass
            .memberProperties
            .sortedBy { it.findAnnotation<Position>()!!.position }
            .map {
                when (val type = it.javaField!!.type) {
                    Byte::class.java -> input.readByte()

                    String::class.java -> input.readString()

                    Array<String>::class.java -> input.readStringArray()

                    else -> throw IllegalArgumentException("The property \"${it.name}\" with type \"${type.simpleName}\" of class \"${packetClass.simpleName}\" has no deserializer defined")
                }
            }
            .toTypedArray()

        return packetClass.primaryConstructor!!.call(*packetFields)
    }

    companion object {
        private val packetsByOpCode = Packet::class
            .deepSealedSubclasses()
            .filter { it.findAnnotation<OpCode>() != null }
            .associateBy { it.findAnnotation<OpCode>()!!.opcode }
    }
}

fun Packet.serialize(): ByteArray {
    val payload = kclass
        .memberProperties
        .sortedBy { it.findAnnotation<Position>()!!.position }
        .map {
            when (val type = it.javaField!!.type.kotlin) {
                Byte::class -> it.serializeAsByteFrom(this)

                String::class -> it.serializeAsStringFrom(this)

                Array<String>::class -> it.serializeAsStringArrayFrom(this)

                else -> throw IllegalArgumentException("The property \"${it.name}\" with type \"${type.simpleName}\" of class \"${this::class.simpleName}\" has no serializer defined")
            }
        }
        .flatten()

    return byteArrayOf(this::class.findAnnotation<OpCode>()!!.opcode) + payload
}
