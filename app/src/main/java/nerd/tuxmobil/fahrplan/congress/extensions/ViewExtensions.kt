@file:JvmName("ViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * See [ViewCompat.requireViewById].
 */
fun <T : View> View.requireViewByIdCompat(@IdRes id: Int): T =
    ViewCompat.requireViewById(this, id)

fun View.applyEdgeToEdgeInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val builder = WindowInsetsCompat.Builder()
        // Insets must be queried and set per individual type: WindowInsetsCompat#getInsets()
        // returns the union of all types in the given mask, so building the result with the
        // combined mask would make every type in it report that same union instead of its own
        // value (e.g. ime() would report max(systemBars, displayCutout, ime) instead of ime).
        for (type in listOf(systemBars(), displayCutout(), ime())) {
            if (typeMask and type != 0) {
                builder.setInsets(type, windowInsets.getInsets(type))
            }
        }
        builder.build()
    }
}

fun View.applyHorizontalInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        view.updateLayoutParams<MarginLayoutParams> {
            leftMargin = insets.left
            rightMargin = insets.right
        }
        windowInsets
    }
}

fun View.applyRightInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        view.updateLayoutParams<MarginLayoutParams> {
            rightMargin = insets.right
        }
        windowInsets
    }
}

/**
 * Grows the view's bottom padding by the IME height so vertically centered content
 * (e.g. via `android:layout_centerInParent`) re-centers above the keyboard instead of
 * staying centered behind it.
 */
fun View.applyImeBottomPadding() {
    val initialPaddingBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val imeBottomInset = windowInsets.getInsets(ime()).bottom
        view.updatePadding(bottom = initialPaddingBottom + imeBottomInset)
        windowInsets
    }
}
