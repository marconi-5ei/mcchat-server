package mcchat.server.helpers

import java.io.ByteArrayOutputStream
import java.io.InputStream

fun terminate(bytes: ByteArray, terminator: Byte = 0): ByteArray {
    return ByteArrayOutputStream().apply {
        write(bytes)
        write(terminator)
    }.toByteArray()
}

fun ByteArrayOutputStream.write(byte: Byte) {
    this.write(byte.toInt())
}

fun InputStream.readString(): String {
    val buffer = ByteArrayOutputStream()

    while (true) {
        val current = this.read()

        if (current == 0)
            break

        buffer.write(current)
    }

    return String(buffer.toByteArray())
}
