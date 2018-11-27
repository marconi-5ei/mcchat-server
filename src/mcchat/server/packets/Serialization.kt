@file:Suppress("unused")

package mcchat.server.packets

import mcchat.server.helpers.getFrom
import mcchat.server.helpers.readString
import mcchat.server.helpers.terminate
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

class Parser(private val input: InputStream) : Iterator<Packet?> {
    override fun hasNext(): Boolean {
        return input.available() > 1
    }

    override fun next(): Packet? {
        @Suppress("UNCHECKED_CAST")
        val packetClass = packetsByOpCode[input.read().toByte()] ?: return null

        val packetFields = mutableListOf<Any>()

        for (property in packetClass.memberProperties)
            when (property.javaField!!.type) {
                Byte::class.java -> packetFields.add(input.read().toByte())

                String::class.java -> packetFields.add(input.readString())

                Array<String>::class.java -> {
                    val strings = mutableListOf<String>()

                    while (true) {
                        input.mark(1)

                        if (input.read().toByte() == ListPacket.TERMINATOR)
                            break

                        input.reset()

                        strings.add(input.readString())
                    }

                    packetFields.add(strings.toTypedArray())
                }
            }

        return packetClass.primaryConstructor!!.call(*packetFields.toTypedArray())
    }

    companion object {
        private val packetsByOpCode = discoverClasses()
            .filter { !it.isSealed }
            .associateBy { (it.companionObjectInstance as OpCoded).opcode }
    }
}


fun serialize(packet: Packet): ByteArray {
    val out = ByteArrayOutputStream()

    out.write((packet::class.companionObjectInstance as OpCoded).opcode)

    for (property in packet::class.memberProperties.reversed())
        when (property.javaField!!.type) {
            Byte::class.java -> out.write(property.getFrom(packet) as Byte)

            String::class.java -> out.write(
                terminate((property.getFrom(packet) as String).toByteArray())
            )

            Array<String>::class.java -> out.write(
                terminate(
                    (property.getFrom(packet) as Array<String>)
                        .map { it.toByteArray() }
                        .map { terminate(it) }
                        .fold(byteArrayOf(), ByteArray::plus),
                    ListPacket.TERMINATOR
                )
            )
        }

    return out.toByteArray()
}
