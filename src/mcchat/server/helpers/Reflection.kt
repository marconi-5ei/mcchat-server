package mcchat.server.helpers

import java.lang.reflect.Field
import kotlin.reflect.jvm.kotlinProperty

fun <T> Field.getFrom(obj: Any): T {
    return this.kotlinProperty?.getter?.call(obj) as T
}
