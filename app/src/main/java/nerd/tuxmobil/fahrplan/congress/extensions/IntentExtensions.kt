package nerd.tuxmobil.fahrplan.congress.extensions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import info.metadude.android.eventfahrplan.commons.temporal.Moment

/**
 * Puts the given [pairs] as a [Bundle] into this [Intent].
 */
fun Intent.withExtras(vararg pairs: Pair<String, Any?>): Intent = putExtras(bundleOf(*pairs))

/**
 * Returns the value of an item previously added with putExtra(String, long),
 * or the [defaultValue] if no item with the [name] was found.
 */
fun Intent.getMomentExtra(name: String, defaultValue: Moment): Moment =
    Moment.ofEpochMilli(getLongExtra(name, defaultValue.toMilliseconds()))

/**
 * Retrieves extended data of type [T] from the intent. See [Intent.getParcelableExtra].
 * To be removed once the androidx.core library offers such a compat wrapper.
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}
