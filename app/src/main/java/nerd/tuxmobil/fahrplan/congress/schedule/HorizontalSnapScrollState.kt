package nerd.tuxmobil.fahrplan.congress.schedule

/**
 * Immutable state for [HorizontalSnapScrollView].
 * Create a copy to mutate values.
 *
 * Acts as a delegate for mathematical calculations for [HorizontalSnapScrollView]
 * so that these can be unit tested easily.
 */
data class HorizontalSnapScrollState(

    val xStart: Int = 0,
    val displayColumnCount: Int = DEFAULT_DISPLAY_COLUMNS_COUNT,
    val roomsCount: Int = NOT_INITIALIZED,
    val columnWidth: Int = DEFAULT_COLUMNS_WIDTH,
    var activeColumnIndex: Int = DEFAULT_ACTIVE_COLUMN_INDEX

) {

    companion object {
        private const val NOT_INITIALIZED = Int.MIN_VALUE
        private const val DEFAULT_DISPLAY_COLUMNS_COUNT = 1
        private const val DEFAULT_COLUMNS_WIDTH = 0
        private const val DEFAULT_ACTIVE_COLUMN_INDEX = 0
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

}
