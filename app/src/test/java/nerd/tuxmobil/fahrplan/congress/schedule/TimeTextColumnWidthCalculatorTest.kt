package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TimeTextColumnWidthCalculatorTest {

    @Test
    fun `calculateWidth returns the minimum width if the text needs less room`() {
        val calculator = TimeTextColumnWidthCalculator(measureText = { 10f })
        // 4 digits + colon = 50, plus padding = 56
        assertThat(calculator.calculateWidth(minimumWidth = 100, horizontalPadding = 6)).isEqualTo(100)
    }

    @Test
    fun `calculateWidth returns the minimum width if the text needs exactly the same room`() {
        val calculator = TimeTextColumnWidthCalculator(measureText = { 10f })
        assertThat(calculator.calculateWidth(minimumWidth = 56, horizontalPadding = 6)).isEqualTo(56)
    }

    @Test
    fun `calculateWidth returns the text width plus padding if the text needs more room`() {
        val calculator = TimeTextColumnWidthCalculator(measureText = { 20f })
        // 4 digits + colon = 100, plus padding = 106
        assertThat(calculator.calculateWidth(minimumWidth = 100, horizontalPadding = 6)).isEqualTo(106)
    }

    @Test
    fun `calculateWidth rounds a fractional text width up`() {
        val calculator = TimeTextColumnWidthCalculator(measureText = { 20.1f })
        // 4 digits + colon = 100.5 -> 101, plus padding = 107
        assertThat(calculator.calculateWidth(minimumWidth = 100, horizontalPadding = 6)).isEqualTo(107)
    }

    @Test
    fun `calculateWidth accounts for the widest digit and the narrower colon`() {
        val calculator = TimeTextColumnWidthCalculator(
            measureText = { text ->
                when (text) {
                    ":" -> 4f
                    "8" -> 12f
                    else -> 10f
                }
            },
        )
        // 4 widest digits + colon = 52, plus padding = 58
        assertThat(calculator.calculateWidth(minimumWidth = 38, horizontalPadding = 6)).isEqualTo(58)
    }

}
