package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import org.junit.Test

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
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth + 1)).isEqualTo(4)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the index of the 1st column to the left if swiping to the right in portrait mode`() {
        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10, displayColumnCount = 1)) {
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = -(scrollThresholdWidth + 1))).isEqualTo(6)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the unmodified current index if the scroll threshold is not exceeded`() {
        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            assertThat(calculateOnTouchColumnIndex(scrollX = 0)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth)).isEqualTo(5)
        }

        with(createState().copy(columnWidth = 993, activeColumnIndex = 5, roomsCount = 10)) {
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
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
    fun `calculateOnTouchColumnIndex returns 0 as the minimum index in portrait mode to prevent scrolling off the screen to the left`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 1,
                activeColumnIndex = 0,
                roomsCount = 5
            )
        ) {
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = scrollThresholdWidth + 1)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns 0 as the minimum index in landscape mode to prevent scrolling off the screen to the left`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 4,
                activeColumnIndex = 0,
                roomsCount = 5
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = columnWidth * 2)).isEqualTo(0)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the last possible index as the maximum value in portrait mode to prevent scrolling off the screen to the right`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 1,
                activeColumnIndex = 4,
                roomsCount = 10
            )
        ) {
            val scrollThresholdWidth = (columnWidth * 0.25).toInt()
            assertThat(calculateOnTouchColumnIndex(scrollX = -scrollThresholdWidth + 1)).isEqualTo(4)
        }
    }

    @Test
    fun `calculateOnTouchColumnIndex returns the last possible index as the maximum value in landscape mode to prevent scrolling off the screen to the right`() {
        with(
            createState().copy(
                columnWidth = 993,
                displayColumnCount = 4,
                activeColumnIndex = 5,
                roomsCount = 10
            )
        ) {
            assertThat(calculateOnTouchColumnIndex(scrollX = -columnWidth * 2)).isEqualTo(6)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns columnIndex x columnWidth`() {
        with(createState().copy(columnWidth = 993)) {
            assertThat(
                calculateScrollToXPosition(
                    measuredWidth = 53622,
                    columnIndex = 1
                )
            ).isEqualTo(993)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns 0 if columnWidth = 0`() {
        with(createState().copy(columnWidth = 0)) {
            assertThat(
                calculateScrollToXPosition(
                    measuredWidth = 53622,
                    columnIndex = 0
                )
            ).isEqualTo(0)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns the minColumnIndex if columnIndex = -1`() {
        with(createState().copy(columnWidth = 993)) {
            assertThat(
                calculateScrollToXPosition(
                    measuredWidth = 53622,
                    columnIndex = -1
                )
            ).isEqualTo(0)
        }
    }

    @Test
    fun `calculateScrollToXPosition returns the maxColumnIndex x columnWidth if columnIndex exceeds maxColumnIndex`() {
        with(createState().copy(columnWidth = 993)) {
            assertThat(
                calculateScrollToXPosition(
                    measuredWidth = 53622,
                    columnIndex = 54
                )
            ).isEqualTo(52629)
        }
    }

    private fun createState() = HorizontalSnapScrollState(
        logging = NoLogging
    )

}
