package nerd.tuxmobil.fahrplan.congress.schedule

import kotlin.math.ceil
import kotlin.math.max

/**
 * Calculates the width of the time column in the main schedule screen.
 *
 * The minimum width does not scale with the font size of the system. The column is therefore
 * widened when needed to keep the time text in a single line.
 */
internal class TimeTextColumnWidthCalculator(

    /**
     * Returns the width of the given text in pixels when rendered in the time column.
     */
    private val measureText: (String) -> Float,

) {

    /**
     * Returns the width in pixels which fits the widest possible time text, e.g. "08:15",
     * including the given [horizontalPadding], but at least the given [minimumWidth].
     */
    fun calculateWidth(minimumWidth: Int, horizontalPadding: Int): Int {
        val widestDigitWidth = ('0'..'9').maxOf { measureText(it.toString()) }
        val widestTextWidth = 4 * widestDigitWidth + measureText(":")
        val requiredWidth = ceil(widestTextWidth).toInt() + horizontalPadding
        return max(minimumWidth, requiredWidth)
    }

}
