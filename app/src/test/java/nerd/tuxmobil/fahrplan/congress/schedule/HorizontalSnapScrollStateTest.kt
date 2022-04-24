package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
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
            HorizontalSnapScrollState(xStart = -1)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("xStart cannot be less then 0 but is -1.")
        }
    }

    @Test
    fun `displayColumnCount is constraint to values greater than 0`() {
        try {
            HorizontalSnapScrollState(displayColumnCount = 0)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("displayColumnCount cannot be 0.")
        }
    }

    @Test
    fun `columnWidth is constraint to values greater than 0`() {
        try {
            HorizontalSnapScrollState(columnWidth = -1)
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("columnWidth cannot be less then 0 but is -1.")
        }
    }

    @Test
    fun `isRoomsCountInitialized returns true if roomCount is greater than 0`() {
        assertThat(createState().copy(roomsCount = 23).isRoomsCountInitialized()).isTrue()
    }

    private fun createState() = HorizontalSnapScrollState()

}
