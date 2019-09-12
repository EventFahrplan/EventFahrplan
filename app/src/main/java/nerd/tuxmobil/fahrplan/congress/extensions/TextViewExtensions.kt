package nerd.tuxmobil.fahrplan.congress.extensions

import android.view.View
import android.widget.TextView

/**
 * Returns the text this TextView is displaying.
 * Sets the text to be displayed or hides this view if the text is empty.
 */
var TextView.textOrHide: CharSequence
    get() = this.text
    set(value) {
        if (value.isEmpty()) {
            visibility = View.GONE
        } else {
            text = value
            visibility = View.VISIBLE
        }
    }
