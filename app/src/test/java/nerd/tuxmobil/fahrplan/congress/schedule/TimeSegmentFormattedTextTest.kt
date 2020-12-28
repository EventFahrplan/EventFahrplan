package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.TimeZone

@RunWith(Parameterized::class)
class TimeSegmentFormattedTextTest(

        private val minutesOfTheDay: Int,
        private val expectedFormattedText: String

) {

    companion object {

        val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT+1")

        private fun scenarioOf(minutesOfTheDay: Int, expectedFormattedText: String) =
                arrayOf(minutesOfTheDay, expectedFormattedText)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: minutes = {0} -> formattedText = {1}")
        fun data() = listOf(
                scenarioOf(minutesOfTheDay = 0, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 120, expectedFormattedText = "03:00"),
                scenarioOf(minutesOfTheDay = 660, expectedFormattedText = "12:00"),
                scenarioOf(minutesOfTheDay = 1425, expectedFormattedText = "00:45")
        )
    }

    @Test
    fun formattedText() {
        TimeZone.setDefault(DEFAULT_TIME_ZONE)
        val segment = TimeSegment(minutesOfTheDay)
        assertThat(segment.formattedText).isEqualTo(expectedFormattedText)
    }

}
