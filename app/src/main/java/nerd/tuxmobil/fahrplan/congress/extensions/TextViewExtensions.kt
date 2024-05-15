@file:JvmName("TextViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.widget.TextView
import androidx.core.view.isVisible

/**
 * Returns the text this TextView is displaying.
 * Sets the text to be displayed or hides this view if the text is empty.
 */
var TextView.textOrHide: CharSequence
    get() = this.text
    set(value) {
        if (value.isEmpty()) {
            isVisible = false
        } else {
            text = value
            isVisible = true
        }
    }
