@file:Suppress("unused")

package mcchat.server.packets

import mcchat.server.helpers.getFrom
import mcchat.server.helpers.nullTerminate
import mcchat.server.helpers.readString
import mcchat.server.helpers.write
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

private fun discoverClasses(clazz: KClass<out Packet> = Packet::class): List<KClass<out Packet>> {
    return clazz.sealedSubclasses.let { it + it.flatMap(::discoverClasses) }
}

class Parser(private val input: InputStream) : Iterator<Packet> {
    override fun hasNext(): Boolean {
        return input.available() > 1
    }

    override fun next(): Packet {
        @Suppress("UNCHECKED_CAST")
        val packetClass = packetsByOpCode[input.read().toByte()]!!

        val packetFields = mutableListOf<Any>()

        for (property in packetClass.memberProperties)
            when (property.javaField!!.type) {
                Byte::class.java -> packetFields.add(input.read().toByte())

                String::class.java -> packetFields.add(input.readString())

                Array<String>::class.java -> {
                    val strings = mutableListOf<String>()

                    while (true) {
                        val current = input.readString()

                        if (current == "")
                            break

                        strings.add(current)
                    }

                    packetFields.add(strings.toTypedArray())
                }
            }

        return packetClass.primaryConstructor!!.call(*packetFields.toTypedArray())
    }

    companion object {
        val packetsByOpCode = discoverClasses()
            .filter { !it.isSealed }
            .associateBy { (it.companionObjectInstance as OpCoded).opcode }
    }
}


fun serialize(packet: Packet): ByteArray {
    val out = ByteArrayOutputStream()

    out.write((packet::class.companionObjectInstance as OpCoded).opcode)

    for (property in packet::class.memberProperties)
        when (property.javaField!!.type) {
            Byte::class.java -> out.write(property.getFrom(packet) as Byte)

            String::class.java -> out.write(
                nullTerminate((property.getFrom(packet) as String).toByteArray())
            )

            Array<String>::class.java -> out.write(
                nullTerminate(
                    (property.getFrom(packet) as Array<String>)
                        .map { it.toByteArray() }
                        .map(::nullTerminate)
                        .reduce(ByteArray::plus)
                )
            )
        }

    return out.toByteArray()
}
