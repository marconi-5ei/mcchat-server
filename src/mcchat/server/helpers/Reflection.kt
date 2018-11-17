package mcchat.server.helpers

import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
fun <T> KProperty1<out Any, Any?>.getFrom(obj: Any): T {
    return this.getter.call(obj) as T
}
