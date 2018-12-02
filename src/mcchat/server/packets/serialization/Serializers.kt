package mcchat.server.packets.serialization

import mcchat.server.helpers.flatten
import mcchat.server.helpers.nullTerminate
import mcchat.server.helpers.terminateWith
import mcchat.server.packets.ListPacket
import kotlin.reflect.KProperty1

internal fun <T> KProperty1<T, *>.serializeAsByteFrom(obj: T): ByteArray {
    return byteArrayOf(this.get(obj) as Byte)
}

fun serializeString(string: String): ByteArray {
    return string.toByteArray().nullTerminate()
}

internal fun <T> KProperty1<T, *>.serializeAsStringFrom(obj: T): ByteArray {
    return serializeString(this.get(obj) as String)
}

internal fun <T, E> KProperty1<T, *>.serializeAsArrayFrom(obj: T, elementSerializer: (E) -> ByteArray): ByteArray {
    @Suppress("UNCHECKED_CAST")
    return (this.get(obj) as Array<E>)
        .map(elementSerializer)
        .flatten()
        .terminateWith(ListPacket.TERMINATOR)
}
