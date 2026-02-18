package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Immutable state for [HorizontalSnapScrollView].
 * Create a copy to mutate values.
 *
 * Acts as a delegate for mathematical calculations for [HorizontalSnapScrollView]
 * so that these can be unit tested easily.
 */
data class HorizontalSnapScrollState(

    val logging: Logging,
    val xStart: Int = 0,
    val displayColumnCount: Int = DEFAULT_DISPLAY_COLUMNS_COUNT,
    val roomsCount: Int = NOT_INITIALIZED,
    val columnWidth: Int = DEFAULT_COLUMNS_WIDTH,
    var activeColumnIndex: Int = DEFAULT_ACTIVE_COLUMN_INDEX

) {

    companion object {
        private const val LOG_TAG = "HorizontalSnapScrollState"
        private const val NOT_INITIALIZED = Int.MIN_VALUE
        private const val DEFAULT_DISPLAY_COLUMNS_COUNT = 1
        private const val DEFAULT_COLUMNS_WIDTH = 0
        private const val DEFAULT_ACTIVE_COLUMN_INDEX = 0

        @VisibleForTesting
        const val SCROLL_THRESHOLD_FACTOR = 0.25
    }

    init {
        check(xStart >= 0) { "xStart cannot be less then 0 but is $xStart." }
        check(displayColumnCount > 0) { "displayColumnCount cannot be 0." }
        check(columnWidth >= 0) { "columnWidth cannot be less then 0 but is $columnWidth." }
        if (activeColumnIndex < 0) {
            activeColumnIndex = DEFAULT_ACTIVE_COLUMN_INDEX
        }
    }

    fun isRoomsCountInitialized() = roomsCount != NOT_INITIALIZED

    @VisibleForTesting
    val maxColumnIndex
        get() = max(roomsCount - displayColumnCount, 0)

    /**
     * Returns the column index calculated based on the given [scrollX] value
     * and the current state of [HorizontalSnapScrollState].
     * @return Returns the calculated index if it is valid, in any other case -1
     */
    fun calculateOnTouchColumnIndex(scrollX: Int): Int {
        val relativeDistance = scrollX - xStart
        var columnIndex = activeColumnIndex

        logging.d(
            LOG_TAG,
            """column width: $columnWidth, scrollX: $scrollX, distance: $relativeDistance, activeColumnIndex: $activeColumnIndex"""
        )

        var columnIndexDelta = calculateRelativeColumnIndexDelta(relativeDistance)

        if (displayColumnCount <= 1 && checkPortraitModeScrollDistance(relativeDistance)) {
            columnIndexDelta = if (relativeDistance > 0) 1 else -1
        }

        logging.d(LOG_TAG, "column distance: ${abs(columnIndexDelta)}")

        columnIndex -= columnIndexDelta

        return if (columnIndex !in 0..maxColumnIndex) -1 else columnIndex
    }

    /**
     * Checks if a given scroll motion is far enough for a scroll
     * to the next column in portrait mode, based on the [columnWidth]
     */
    fun checkPortraitModeScrollDistance(relativeDistance: Int) =
        abs(relativeDistance) > columnWidth * SCROLL_THRESHOLD_FACTOR

    /**
     * Calculates the relative column delta based on the [relativeDistance] and
     * the current value of [columnWidth].
     */
    fun calculateRelativeColumnIndexDelta(relativeDistance: Int): Int {
        val absoluteColumnDelta = abs(relativeDistance.toFloat()) / columnWidth
        return if (relativeDistance > 0)
            absoluteColumnDelta.roundToInt()
        else {
            -(absoluteColumnDelta).roundToInt()
        }
    }

    /**
     * Returns the horizontal position calculated based on the given [columnIndex]
     * and the current values of [maxColumnIndex] and [columnWidth].
     */
    fun calculateScrollToXPosition(columnIndex: Int): Int {
        val constraintColumnIndex = when {
            columnIndex < 0 -> 0
            columnIndex > maxColumnIndex -> maxColumnIndex
            else -> columnIndex
        }
        return constraintColumnIndex * columnWidth
    }

    /**
     * Returns the column width calculated based on the given [measuredWidth]
     * and the current value of [displayColumnCount].
     */
    fun calculateColumnWidth(measuredWidth: Int): Int {
        return (measuredWidth.toFloat() / displayColumnCount).roundToInt()
    }

}
