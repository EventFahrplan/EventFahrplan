package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatterFormattedTime24HourTest.TestParameter.Companion.parse
import info.metadude.android.eventfahrplan.commons.testing.withTimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.threeten.bp.ZoneOffset

/**
 * Covers the time zone aware time rendering of [DateFormatter.getFormattedTime24Hour].
 * - Iterating all valid time zone offsets
 * - Iterating all hours of a day
 * - Testing with summer and winter time
 *
 * Regardless which time zone is set at the device always the time zone of the event/session will
 * be rendered.
 *
 * TODO: TechDebt: Once the app offers an option to render sessions in the device time zone this test must be adapted.
 */
@RunWith(Parameterized::class)
class DateFormatterFormattedTime24HourTest(

    private val timeZoneId: String

) {

    companion object {

        private val timeZoneOffsets = -12..14

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: timeZoneId = {0}")
        fun data() = timeZoneOffsets.map { arrayOf("GMT$it") }

    }

    @Test
    fun `getFormattedTime24Hour 2021-03-27`() = testEach(listOf(
        "2021-03-27T00:01:00+01:00" to "00:01",
        "2021-03-27T01:00:00+01:00" to "01:00",
        "2021-03-27T02:00:00+01:00" to "02:00",
        "2021-03-27T03:00:00+01:00" to "03:00",
        "2021-03-27T04:00:00+01:00" to "04:00",
        "2021-03-27T05:00:00+01:00" to "05:00",
        "2021-03-27T06:00:00+01:00" to "06:00",
        "2021-03-27T07:00:00+01:00" to "07:00",
        "2021-03-27T08:00:00+01:00" to "08:00",
        "2021-03-27T09:00:00+01:00" to "09:00",
        "2021-03-27T10:00:00+01:00" to "10:00",
        "2021-03-27T11:00:00+01:00" to "11:00",
        "2021-03-27T12:00:00+01:00" to "12:00",
        "2021-03-27T13:00:00+01:00" to "13:00",
        "2021-03-27T14:00:00+01:00" to "14:00",
        "2021-03-27T15:00:00+01:00" to "15:00",
        "2021-03-27T16:00:00+01:00" to "16:00",
        "2021-03-27T17:00:00+01:00" to "17:00",
        "2021-03-27T18:00:00+01:00" to "18:00",
        "2021-03-27T19:00:00+01:00" to "19:00",
        "2021-03-27T20:00:00+01:00" to "20:00",
        "2021-03-27T21:00:00+01:00" to "21:00",
        "2021-03-27T22:00:00+01:00" to "22:00",
        "2021-03-27T23:00:00+01:00" to "23:00",
        "2021-03-27T23:59:00+01:00" to "23:59",
    ))

    @Test
    fun `getFormattedTime24Hour 2021-03-28`() = testEach(listOf(
        "2021-03-28T00:01:00+02:00" to "00:01",
        "2021-03-28T01:00:00+02:00" to "01:00",
        "2021-03-28T02:00:00+02:00" to "02:00", // TechDebt: Daylight saving time is ignored here.
        "2021-03-28T03:00:00+02:00" to "03:00",
        "2021-03-28T04:00:00+02:00" to "04:00",
        "2021-03-28T05:00:00+02:00" to "05:00",
        "2021-03-28T06:00:00+02:00" to "06:00",
        "2021-03-28T07:00:00+02:00" to "07:00",
        "2021-03-28T08:00:00+02:00" to "08:00",
        "2021-03-28T09:00:00+02:00" to "09:00",
        "2021-03-28T10:00:00+02:00" to "10:00",
        "2021-03-28T11:00:00+02:00" to "11:00",
        "2021-03-28T12:00:00+02:00" to "12:00",
        "2021-03-28T13:00:00+02:00" to "13:00",
        "2021-03-28T14:00:00+02:00" to "14:00",
        "2021-03-28T15:00:00+02:00" to "15:00",
        "2021-03-28T16:00:00+02:00" to "16:00",
        "2021-03-28T17:00:00+02:00" to "17:00",
        "2021-03-28T18:00:00+02:00" to "18:00",
        "2021-03-28T19:00:00+02:00" to "19:00",
        "2021-03-28T20:00:00+02:00" to "20:00",
        "2021-03-28T21:00:00+02:00" to "21:00",
        "2021-03-28T22:00:00+02:00" to "22:00",
        "2021-03-28T23:00:00+02:00" to "23:00",
        "2021-03-28T23:59:00+02:00" to "23:59",
    ))

    private fun testEach(pairs: List<Pair<String, String>>) {
        pairs.forEach { (dateTime, expectedFormattedTime) ->
            val (moment, offset) = parse(dateTime)
            withTimeZone(timeZoneId) {
                val formattedTime = DateFormatter.newInstance(useDeviceTimeZone = false).getFormattedTime24Hour(moment, offset)
                assertThat(formattedTime).isEqualTo(expectedFormattedTime)
            }
        }
    }

    private data class TestParameter(val moment: Moment, val offset: ZoneOffset) {

        companion object {

            fun parse(dateTime: String) = TestParameter(
                parseMoment(dateTime),
                parseTimeZoneOffset(dateTime)
            )

            private fun parseMoment(text: String): Moment {
                val milliseconds = DateParser.parseDateTime(text)
                return Moment.ofEpochMilli(milliseconds)
            }

            private fun parseTimeZoneOffset(text: String): ZoneOffset {
                val seconds = DateParser.parseTimeZoneOffset(text)
                return ZoneOffset.ofTotalSeconds(seconds)
            }

        }
    }

}
