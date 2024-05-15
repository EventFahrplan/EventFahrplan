@file:JvmName("Strings")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml

/**
 * Converts this HTML string into a displayable styled text.
 *
 * See also: [android.text.Html.fromHtml]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toSpanned(
        flags: Int = HtmlCompat.FROM_HTML_MODE_LEGACY
): Spanned = parseAsHtml(flags)
