package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.threeten.bp.OffsetDateTime
import java.util.TimeZone

class TimeSegmentFormattedTextTest {

    companion object {

        val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT+1")

        private fun scenarioOf(minutesOfTheDay: Int, expectedFormattedText: String) =
                arrayOf(minutesOfTheDay, expectedFormattedText)

        @JvmStatic
        fun data() = listOf(
                scenarioOf(minutesOfTheDay = 0, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 1, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 2, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 3, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 4, expectedFormattedText = "01:00"),
                scenarioOf(minutesOfTheDay = 5, expectedFormattedText = "01:05"),
                scenarioOf(minutesOfTheDay = 6, expectedFormattedText = "01:05"),
                scenarioOf(minutesOfTheDay = 7, expectedFormattedText = "01:05"),
                scenarioOf(minutesOfTheDay = 8, expectedFormattedText = "01:05"),
                scenarioOf(minutesOfTheDay = 9, expectedFormattedText = "01:05"),
                scenarioOf(minutesOfTheDay = 10, expectedFormattedText = "01:10"),
                scenarioOf(minutesOfTheDay = 120, expectedFormattedText = "03:00"),
                scenarioOf(minutesOfTheDay = 660, expectedFormattedText = "12:00"),
                scenarioOf(minutesOfTheDay = 1425, expectedFormattedText = "00:45")
        )
    }

    @ParameterizedTest(name = "{index}: minutes = {0} -> formattedText = {1}")
    @MethodSource("data")
    fun formattedText(
        minutesOfTheDay: Int,
        expectedFormattedText: String
    ) {
        TimeZone.setDefault(DEFAULT_TIME_ZONE)
        val zoneOffsetNow = OffsetDateTime.now().offset
        val moment = Moment.now().startOfDay().plusMinutes(minutesOfTheDay.toLong())
        val segment = TimeSegment.ofMoment(moment)
        assertThat(segment.getFormattedText(zoneOffsetNow, useDeviceTimeZone = false)).isEqualTo(expectedFormattedText)
    }

}
