@file:JvmName("ViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.view.View
import android.view.View.CONTENT_SENSITIVITY_SENSITIVE
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

/**
 * See [View.setContentSensitivity].
 */
fun View.enableContentSensitivitySensitive() {
    if (SDK_INT >= VANILLA_ICE_CREAM) {
        contentSensitivity = CONTENT_SENSITIVITY_SENSITIVE
    }
}

fun View.applyEdgeToEdgeInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        WindowInsetsCompat.Builder()
            .setInsets(typeMask, insets)
            .build()
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

fun View.applyBottomPadding(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        view.updatePadding(bottom = insets.bottom)
        windowInsets
    }
}

fun View.applyHorizontalInsetsAndBottomPadding(
    typeMask: Int = systemBars() or displayCutout() or ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        view.updateLayoutParams<MarginLayoutParams> {
            leftMargin = insets.left
            rightMargin = insets.right
        }
        view.updatePadding(bottom = insets.bottom)
        windowInsets
    }
}
