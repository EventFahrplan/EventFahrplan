package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.schedule.HorizontalSnapScrollState.Companion.SCROLL_THRESHOLD_FACTOR
import nerd.tuxmobil.fahrplan.congress.schedule.HorizontalSnapScrollView.Companion.SWIPE_DISTANCE_THRESHOLD
import nerd.tuxmobil.fahrplan.congress.schedule.HorizontalSnapScrollView.Companion.SWIPE_VELOCITY_THRESHOLD
import org.junit.jupiter.api.Test

class HorizontalSnapScrollStateTest {

    @Test
    fun `assert default values`() {
        with(createState()) {
            assertThat(xStart).isEqualTo(0)
            assertThat(displayColumnCount).isEqualTo(1)
            assertThat(roomsCount).isEqualTo(Int.MIN_VALUE)
            assertThat(columnWidth).isEqualTo(0)
            assertThat(activeColumnIndex).isEqualTo(0)
            assertThat(isRoomsCountInitialized()).isEqualTo(false)
        }
    }

    @Test
    fun `activeColumnIndex is reset to its minimum value`() {
        assertThat(createState().copy(activeColumnIndex = -1).activeColumnIndex).isEqualTo(0)
    }

    @Test
    fun `xStart is constraint to positive values`() {
        try {
            HorizontalSnapScrollState(NoLogging, xStart = -1)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("xStart cannot be less then 0 but is -1.")
        }
    }

    @Test
    fun `displayColumnCount is constraint to values greater than 0`() {
        try {
            HorizontalSnapScrollState(NoLogging, displayColumnCount = 0)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("displayColumnCount cannot be 0.")
        }
    }

    @Test
    fun `columnWidth is constraint to values greater than 0`() {
        try {
            HorizontalSnapScrollState(NoLogging, columnWidth = -1)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("columnWidth cannot be less then 0 but is -1.")
        }
    }

    @Test
    fun `isRoomsCountInitialized returns true if roomCount is greater than 0`() {
        assertThat(createState().copy(roomsCount = 23).isRoomsCountInitialized()).isTrue()
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 1st column to the right if swiping to the left in portrait mode`() {
        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10, displayColumnCount = 1)) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth + 1)).isEqualTo(4)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 1st column to the left if swiping to the right in portrait mode`() {
        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10, displayColumnCount = 1)) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = -(scrollThresholdWidth + 1))).isEqualTo(6)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the unmodified current index if the scroll threshold is not exceeded`() {
        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            assertThat(calculateOnTouchColumnIndex(scrollX = 0)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = -scrollThresholdWidth)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            assertThat(calculateOnTouchColumnIndex(scrollX = 1)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -1)).isEqualTo(5)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 2nd column to the right for a two column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -(columnWidth * 2))).isEqualTo(52)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 2nd column to the left for a two column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = columnWidth * 2)).isEqualTo(48)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 1st column to the right for a half column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -(columnWidth * 0.5).toInt())).isEqualTo(51)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 1st column to the left for a half column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = (columnWidth * 0.5).toInt())).isEqualTo(49)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the current index for a less than half leftwards column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -(columnWidth * 0.49).toInt())).isEqualTo(50)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the current index for a less than half rightwards column swipe in landscape mode`() {
        with(
            createState().copy(
                columnWidth = 1000,
                displayColumnCount = 4,
                activeColumnIndex = 50,
                roomsCount = 100
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = (columnWidth * 0.49).toInt())).isEqualTo(50)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns -1 if the target index is lower than 0 in portrait mode to prevent scrolling off the screen to the left`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 1,
                activeColumnIndex = 0,
                roomsCount = 5
            )
        ) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth + 1)).isEqualTo(-1)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns -1 if the target index is lower than 0 in landscape mode to prevent scrolling off the screen to the left`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 4,
                activeColumnIndex = 0,
                roomsCount = 5
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = columnWidth * 2)).isEqualTo(-1)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns -1 if the target index is higher than the max allowed in portrait mode to prevent scrolling off the screen to the right`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 1,
                activeColumnIndex = 9,
                roomsCount = 10
            )
        ) {
            val scrollThresholdWidth = (columnWidth * SCROLL_THRESHOLD_FACTOR).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = -scrollThresholdWidth - 1)).isEqualTo(-1)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns -1 if the target index is higher than the max allowed in landscape mode to prevent scrolling off the screen to the right`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 4,
                activeColumnIndex = 5,
                roomsCount = 10
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -columnWidth * 2)).isEqualTo(-1)
        }
    }

    @Test
    fun `checkPortraitModeScrollDistance returns false if the absolute distance is lower than the threshold`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance((SCROLL_THRESHOLD_FACTOR * 1000).toInt() - 1)).isEqualTo(false)
        }
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance(-(SCROLL_THRESHOLD_FACTOR * 1000).toInt() + 1)).isEqualTo(false)
        }
    }

    @Test
    fun `checkPortraitModeScrollDistance returns false if the absolute distance is equal to the threshold`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance((SCROLL_THRESHOLD_FACTOR * 1000).toInt())).isEqualTo(false)
        }
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance(-(SCROLL_THRESHOLD_FACTOR * 1000).toInt())).isEqualTo(false)
        }
    }

    @Test
    fun `checkPortraitModeScrollDistance returns true if the absolute distance is higher than the threshold`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance((SCROLL_THRESHOLD_FACTOR * 1000).toInt() + 1)).isEqualTo(
                true
            )
        }
        with(createState().copy(columnWidth = 1000)) {
            assertThat(checkPortraitModeScrollDistance(-(SCROLL_THRESHOLD_FACTOR * 1000).toInt() - 1)).isEqualTo(
                true
            )
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns correct value for positive Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(1000)).isEqualTo(1)
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns correct value for negative Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(-1000)).isEqualTo(-1)
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns rounds up to correct value for positive Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(500)).isEqualTo(1)
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns rounds up to correct value for negative Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(-500)).isEqualTo(-1)
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns rounds down to correct value for positive Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(466)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateRelativeColumnIndexDelta returns rounds down to correct value for negative Distances`() {
        with(createState().copy(columnWidth = 1000)) {
            assertThat(calculateRelativeColumnIndexDelta(466)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns columnIndex x columnWidth`() {
        with(createState().copy(columnWidth = 993)) {
            assertThat(calculateScrollToXPosition(columnIndex = 1)).isEqualTo(993)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns 0 if columnWidth = 0`() {
        with(createState().copy(columnWidth = 0)) {
            assertThat(calculateScrollToXPosition(columnIndex = 0)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns the minColumnIndex if columnIndex = -1`() {
        with(createState().copy(columnWidth = 993)) {
            assertThat(calculateScrollToXPosition(columnIndex = -1)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns the maxColumnIndex x columnWidth if columnIndex exceeds maxColumnIndex`() {
        with(createState().copy(columnWidth = 993, roomsCount = 10, displayColumnCount = 1)) {
            assertThat(calculateScrollToXPosition(columnIndex = 15)).isEqualTo(8937)
        }
    }

    @Test
    fun `getMaxColumnIndex returns the most right possible columnIndex in portrait mode`() {
        with(createState().copy(roomsCount = 10, displayColumnCount = 1)) {
            assertThat(maxColumnIndex).isEqualTo(9)
        }
    }

    @Test
    fun `getMaxColumnIndex returns the most right possible columnIndex in landscape mode`() {
        with(createState().copy(roomsCount = 10, displayColumnCount = 4)) {
            assertThat(maxColumnIndex).isEqualTo(6)
        }
    }

    @Test
    fun `getMaxColumnIndex returns 0 as the minimum Value in portrait mode`() {
        with(createState().copy(roomsCount = 0, displayColumnCount = 1)) {
            assertThat(maxColumnIndex).isEqualTo(0)
        }
    }

    @Test
    fun `getMaxColumnIndex returns 0 as the minimum Value in landscape mode`() {
        with(createState().copy(roomsCount = 0, displayColumnCount = 4)) {
            assertThat(maxColumnIndex).isEqualTo(0)
        }
    }

    @Test
    fun `calculateColumnWidth returns 0 if measuredWidth = 0`() {
        with(createState()) {
            assertThat(calculateColumnWidth(measuredWidth = 0)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateColumnWidth returns 1 if measuredWidth = 1`() {
        with(createState().copy(displayColumnCount = 1)) {
            assertThat(calculateColumnWidth(measuredWidth = 1)).isEqualTo(1)
        }
    }

    @Test
    fun `calculateColumnWidth returns 993 if for a Pixel 6 in portrait mode`() {
        with(createState().copy(displayColumnCount = 1)) {
            assertThat(calculateColumnWidth(measuredWidth = 993)).isEqualTo(993)
        }
    }

    @Test
    fun `calculateColumnWidth returns 555 for a Pixel 6 in landscape mode`() {
        with(createState().copy(displayColumnCount = 4)) {
            assertThat(calculateColumnWidth(measuredWidth = 2219)).isEqualTo(555)
        }
    }

    @Test
    fun `isFastEnough returns false if the absolute velocity is less than the threshold`() {
        assertThat(isFastEnough((SWIPE_VELOCITY_THRESHOLD - 1).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(false)
        assertThat(isFastEnough(-(SWIPE_VELOCITY_THRESHOLD - 1).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(false)
    }

    @Test
    fun `isFastEnough returns false if the absolute velocity is equal to the threshold`() {
        assertThat(isFastEnough((SWIPE_VELOCITY_THRESHOLD).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(false)
        assertThat(isFastEnough(-(SWIPE_VELOCITY_THRESHOLD).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(false)
    }

    @Test
    fun `isFastEnough returns true if the absolute velocity is greater than the threshold`() {
        assertThat(isFastEnough((SWIPE_VELOCITY_THRESHOLD + 1).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(true)
        assertThat(isFastEnough(-(SWIPE_VELOCITY_THRESHOLD + 1).toFloat(), SWIPE_VELOCITY_THRESHOLD)).isEqualTo(true)
    }

    @Test
    fun `isLongEnough returns false if the absolute distance is less than the threshold`() {
        assertThat(isLongEnough(+(SWIPE_DISTANCE_THRESHOLD).toFloat(), +1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(false)
        assertThat(isLongEnough(-(SWIPE_DISTANCE_THRESHOLD).toFloat(), -1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(false)
    }

    @Test
    fun `isLongEnough returns false if the absolute distance is equal to the threshold`() {
        assertThat(isLongEnough(+(SWIPE_DISTANCE_THRESHOLD + 1).toFloat(), +1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(false)
        assertThat(isLongEnough(-(SWIPE_DISTANCE_THRESHOLD + 1).toFloat(), -1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(false)
    }

    @Test
    fun `isLongEnough returns true if the absolute distance is greater than the threshold`() {
        assertThat(isLongEnough(+(SWIPE_DISTANCE_THRESHOLD + 2).toFloat(), +1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(true)
        assertThat(isLongEnough(-(SWIPE_DISTANCE_THRESHOLD + 2).toFloat(), -1.0f, SWIPE_DISTANCE_THRESHOLD)).isEqualTo(true)
    }

    private fun createState() = HorizontalSnapScrollState(
        logging = NoLogging
    )

}
