package mcchat.server.helpers

import java.io.ByteArrayOutputStream

fun nullTerminate(bytes: ByteArray): ByteArray {
    return ByteArrayOutputStream().apply {
        write(bytes)
        write(byteArrayOf(0))
    }.toByteArray()
}

fun ByteArrayOutputStream.write(byte: Byte) {
    this.write(byteArrayOf(byte))
}
