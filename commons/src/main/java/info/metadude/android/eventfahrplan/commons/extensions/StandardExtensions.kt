@file:JvmName("StandardExtensions")

package info.metadude.android.eventfahrplan.commons.extensions

/**
 * Calls the specified function [block] with `this` value as its argument if [the receiver][this]
 * is `false` and returns `this` value.
 */
inline fun <Boolean> Boolean.onFailure(block: (Boolean) -> Unit): Boolean {
    if (this == false) {
        block(this)
    }
    return this
}
