@file:JvmName("TextViewExtensions")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.text.method.MovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.text.set
import androidx.core.text.toSpannable
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

/**
 * Sets the given [plainLinkUrl] and the optional [urlTitle] as a clickable link to this [TextView].
 */
@JvmOverloads
fun TextView.setLinkText(
        plainLinkUrl: String,
        urlTitle: String? = null,
        movementMethod: MovementMethod,
        @ColorInt linkTextColor: Int,
) {
    val title = urlTitle ?: plainLinkUrl
    val linkText = title.toSpannable().apply { set(0, title.length, URLSpan(plainLinkUrl)) }
    setText(linkText, TextView.BufferType.SPANNABLE)
    setMovementMethod(movementMethod)
    setLinkTextColor(linkTextColor)
}
