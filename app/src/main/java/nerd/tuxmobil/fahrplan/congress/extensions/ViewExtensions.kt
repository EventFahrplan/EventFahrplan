@file:JvmName("ViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import nerd.tuxmobil.fahrplan.congress.R

/**
 * See [ViewCompat.requireViewById].
 */
fun <T : View> View.requireViewByIdCompat(@IdRes id: Int): T =
    ViewCompat.requireViewById(this, id)


fun View.applyEdgeToEdgeInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
    propagateInsets: Boolean = false,
    viewToApplyTo: View = this,
    block: MarginLayoutParams.(InsetsAccumulator) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)

        val initialTop = if (view.getTag(R.id.initial_margin_top) != null) {
            view.getTag(R.id.initial_margin_top) as Int
        } else {
            view.setTag(R.id.initial_margin_top, view.marginTop)
            view.marginTop
        }

        val initialBottom = if (view.getTag(R.id.initial_margin_bottom) != null) {
            view.getTag(R.id.initial_margin_bottom) as Int
        } else {
            view.setTag(R.id.initial_margin_bottom, view.marginBottom)
            view.marginBottom
        }

        val initialLeft = if (view.getTag(R.id.initial_margin_left) != null) {
            view.getTag(R.id.initial_margin_left) as Int
        } else {
            view.setTag(R.id.initial_margin_left, view.marginLeft)
            view.marginLeft
        }

        val initialRight = if (view.getTag(R.id.initial_margin_right) != null) {
            view.getTag(R.id.initial_margin_right) as Int
        } else {
            view.setTag(R.id.initial_margin_right, view.marginRight)
            view.marginRight
        }

        val accumulator = InsetsAccumulator(
            initialTop,
            insets.top,
            initialBottom,
            insets.bottom,
            initialLeft,
            insets.left,
            initialRight,
            insets.right,
        )

        viewToApplyTo.updateLayoutParams<MarginLayoutParams> {
            apply { block(accumulator) }
        }

        if (propagateInsets) windowInsets else CONSUMED
    }
}

fun View.applyTopAndSideInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
    propagateInsets: Boolean = false,
    viewToApplyTo: View = this,
) = applyEdgeToEdgeInsets(typeMask, propagateInsets, viewToApplyTo) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
    topMargin = insets.top
}

fun View.applySideInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
    propagateInsets: Boolean = false,
    viewToApplyTo: View = this,
) = applyEdgeToEdgeInsets(typeMask, propagateInsets, viewToApplyTo) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
}

fun View.applyBottomAndSideInsets(
    typeMask: Int = systemBars() or displayCutout() or ime(),
    propagateInsets: Boolean = false,
    viewToApplyTo: View = this,
) = applyEdgeToEdgeInsets(typeMask, propagateInsets, viewToApplyTo) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
    bottomMargin = insets.bottom
}

data class InsetsAccumulator(
    private val initialTop: Int,
    private val insetTop: Int,
    private val initialBottom: Int,
    private val insetBottom: Int,
    private val initialLeft: Int,
    private val insetLeft: Int,
    private val initialRight: Int,
    private val insetRight: Int,
) {
    val top: Int
        get() = initialTop + insetTop

    val bottom: Int
        get() = initialBottom + insetBottom

    val left: Int
        get() = initialLeft + insetLeft

    val right: Int
        get() = initialRight + insetRight
}
