@file:JvmName("KClassUtils")

package info.metadude.android.eventfahrplan.commons.testing

import java.lang.reflect.Method
import kotlin.reflect.KClass

fun KClass<*>.withMethod(methodName: String, block: Method.() -> Unit) {
    val method = java.getDeclaredMethod(methodName)
    val wasAccessible = method.isAccessible
    try {
        method.isAccessible = true
        method.block()
    } finally {
        method.isAccessible = wasAccessible
    }
}
