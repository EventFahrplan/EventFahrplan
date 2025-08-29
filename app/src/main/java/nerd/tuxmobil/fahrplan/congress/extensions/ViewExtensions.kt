@file:JvmName("ViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat

/**
 * See [ViewCompat.requireViewById].
 */
fun <T : View> View.requireViewByIdCompat(@IdRes id: Int): T =
    ViewCompat.requireViewById(this, id)
