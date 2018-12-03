@file:Suppress("unused")

package mcchat.server.packets.serialization

import mcchat.server.helpers.Position
import mcchat.server.helpers.deepSealedSubclasses
import mcchat.server.helpers.flatten
import mcchat.server.helpers.kclass
import mcchat.server.packets.OpCoded
import mcchat.server.packets.Packet
import java.io.InputStream
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

// TODO Consider replacing reflection with code generation (define packets as lists of pairs (<name> to <type>::class) and ancestors to inherit from)

class Parser(private val input: InputStream) : Iterator<Packet?> {
    override fun hasNext(): Boolean {
        return input.available() > 0
    }

    override fun next(): Packet? {
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

                    Array<String>::class.java -> input.readArray(InputStream::readString)

                    else -> throw IllegalArgumentException("The property \"${it.name}\" with type \"${type.simpleName}\" of class \"${packetClass.simpleName}\" has no deserializer defined")
                }
            }
            .toTypedArray()

        return packetClass.primaryConstructor!!.call(*packetFields)
    }

    companion object {
        private val packetsByOpCode = Packet::class.deepSealedSubclasses()
            .associateBy { (it.companionObjectInstance as OpCoded).opcode }
    }
}

fun serialize(packet: Packet): ByteArray {
    val payload = packet
        .kclass
        .memberProperties
        .sortedBy { it.findAnnotation<Position>()!!.position }
        .map {
            when (val type = it.javaField!!.type.kotlin) {
                Byte::class -> it.serializeAsByteFrom(packet)

                String::class -> it.serializeAsStringFrom(packet)

                Array<String>::class -> it.serializeAsArrayFrom(packet, ::serializeString)

                else -> throw IllegalArgumentException("The property \"${it.name}\" with type \"${type.simpleName}\" of class \"${packet::class.simpleName}\" has no serializer defined")
            }
        }
        .flatten()

    return byteArrayOf((packet::class.companionObjectInstance as OpCoded).opcode) + payload
}
