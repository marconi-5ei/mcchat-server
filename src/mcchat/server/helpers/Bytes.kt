package mcchat.server.helpers

import java.io.ByteArrayOutputStream

fun ByteArray.terminateWith(terminator: Byte): ByteArray {
    return ByteArrayOutputStream().also {
        it.write(this)
        it.write(terminator)
    }.toByteArray()
}

fun ByteArray.nullTerminate(): ByteArray {
    return this.terminateWith(0)
}

fun Iterable<ByteArray>.flatten(): ByteArray {
    return this
        .map { it.toTypedArray() }
        .toTypedArray()
        .flatten()
        .toByteArray()
}

fun ByteArrayOutputStream.write(byte: Byte) {
    this.write(byte.toInt())
}
