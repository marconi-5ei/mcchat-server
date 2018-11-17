package mcchat.server.helpers

import java.io.ByteArrayOutputStream
import java.io.InputStream

fun nullTerminate(bytes: ByteArray): ByteArray {
    return ByteArrayOutputStream().apply {
        write(bytes)
        write(byteArrayOf(0))
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
