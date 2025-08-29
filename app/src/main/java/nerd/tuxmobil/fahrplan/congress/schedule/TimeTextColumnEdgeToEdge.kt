package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.children
import androidx.core.view.isNotEmpty
import androidx.core.view.updateLayoutParams
import nerd.tuxmobil.fahrplan.congress.R

class TimeTextColumnEdgeToEdge(private val onTimeTextColumnLayoutWidth: () -> Int) {

    private companion object {
        const val EXTENSION_TAG = "time_column_bottom_extension"
    }

    var leftWindowInset: Int = 0
        private set

    private var bottomWindowInset: Int = 0

    fun findBottomViewExtension(timeTextColumn: LinearLayout): View? {
        return timeTextColumn.children.find { it.tag == EXTENSION_TAG }
    }

    /**
     * Applies the start inset to the [timeTextColumn] by moving the text views inwards while
     * expanding their width to the left edge of the screen. By this, the time text is still
     * readable when a display cutout covers only the column background and the lines.
     * Also tracks bottom insets for creating a matching bottom extension to safeContentPadding().
     */
    fun applyInsets(view: View, timeTextColumn: LinearLayout) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(systemBars() or displayCutout() or ime())
            val previousLeftInset = leftWindowInset
            val previousBottomInset = bottomWindowInset
            leftWindowInset = insets.left
            bottomWindowInset = insets.bottom

            // If insets changed, update existing timeTextViews that may have been created with wrong width
            if (previousLeftInset != leftWindowInset) {
                timeTextColumn.children.forEach { timeTextView ->
                    val textView = timeTextView.findViewById<TextView>(R.id.schedule_time_column_time_text_view)
                    textView?.updateLayoutParams {
                        width = onTimeTextColumnLayoutWidth() + leftWindowInset
                    }
                }
            }

            // If bottom insets changed, update the bottom extension
            if (previousBottomInset != bottomWindowInset) {
                appendBottomViewExtension(timeTextColumn)
            }

            windowInsets
        }
    }

    /**
     * Updates the bottom extension of the [timeTextColumn] to match the safeContentPadding() in Compose.
     * This creates a bottom padding that matches the bottom window inset.
     */
    fun appendBottomViewExtension(timeTextColumn: LinearLayout) {
        // Remove any existing bottom extension view
        timeTextColumn.children.toList().forEach { child ->
            if (child.tag == EXTENSION_TAG) {
                timeTextColumn.removeView(child)
            }
        }

        // Add new bottom extension view if bottom inset exists
        if (bottomWindowInset > 0 && timeTextColumn.isNotEmpty()) {
            val extensionView = View(timeTextColumn.context).apply {
                tag = EXTENSION_TAG
                setBackgroundColor(ContextCompat.getColor(context, R.color.schedule_time_column_item_background_normal))
                layoutParams = LinearLayout.LayoutParams(
                    onTimeTextColumnLayoutWidth() + leftWindowInset,
                    bottomWindowInset,
                )
            }
            timeTextColumn.addView(extensionView)
        }
    }

}
