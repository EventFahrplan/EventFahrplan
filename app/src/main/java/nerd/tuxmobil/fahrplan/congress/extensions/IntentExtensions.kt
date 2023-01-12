package nerd.tuxmobil.fahrplan.congress.extensions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf

/**
 * Puts the given [pairs] as a [Bundle] into this [Intent].
 */
fun Intent.withExtras(vararg pairs: Pair<String, Any?>): Intent = putExtras(bundleOf(*pairs))

/**
 * Retrieves extended data of type [T] from the intent. See [Intent.getParcelableExtra].
 * To be removed once the androidx.core library offers such a compat wrapper.
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}
