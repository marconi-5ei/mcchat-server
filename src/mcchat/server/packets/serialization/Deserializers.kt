package mcchat.server.packets.serialization

import mcchat.server.helpers.write
import mcchat.server.packets.ListPacket
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

fun InputStream.readByte(): Byte {
    val byte = this.read()

    if (byte == -1)
        throw IOException("Stream ended while reading")

    return byte.toByte()
}

fun InputStream.readUntil(byte: Byte): ByteArray {
    val buffer = ByteArrayOutputStream()

    while (true) {
        val current = this.readByte()

        if (current == byte)
            break

        buffer.write(current)
    }

    return buffer.toByteArray()
}

fun InputStream.readString(): String {
    return String(this.readUntil(0))
}

inline fun <reified T> InputStream.readArray(elementDeserializer: (InputStream).() -> T): Array<T> {
    val serializedList = ByteArrayInputStream(this.readUntil(ListPacket.TERMINATOR))

    val out = mutableListOf<T>()

    while (serializedList.available() != 0)
        out.add(serializedList.elementDeserializer())

    return out.toTypedArray()
}
