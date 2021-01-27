package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.withTimeZone
import info.metadude.android.eventfahrplan.network.temporal.DateParser
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.threeten.bp.ZoneOffset

/**
 * Covers ScrollAmountCalculator.calculateScrollAmount(Conference, Session, int).
 * Ensures correct behavior with different time zone offsets.
 */
@RunWith(Parameterized::class)
class ScrollAmountCalculatorTimeZoneOffsetTest(

        private val deviceTimeZoneId: String,
        private val sessionStartsAtDateTimeIso8601: String,
        private val conferenceStartedHoursAgo: Int,
        private val expectedScrollAmount: Int

) {

    companion object {

        private const val BOX_HEIGHT = 34 // Pixel 2 portrait mode
        private val timeZoneOffsets = -12..14

        private fun scenarioOf(
                deviceTimeZoneId: String,
                sessionStartsAtDateTimeIso8601: String,
                startedHoursAgo: Int,
                expectedScrollAmount: Int
        ) =
                arrayOf(deviceTimeZoneId, sessionStartsAtDateTimeIso8601, startedHoursAgo, expectedScrollAmount)

        private fun startsNowScenarioOf(deviceTimeZoneId: String) =
                scenarioOf(deviceTimeZoneId, "2019-08-21T11:00:00+02:00", startedHoursAgo = 0, expectedScrollAmount = 0)

        private fun startedBeforeScenarioOf(deviceTimeZoneId: String) =
                scenarioOf(deviceTimeZoneId, "2019-08-22T02:00:00+02:00", startedHoursAgo = 7, expectedScrollAmount = 2856)

        private fun winterSummerScenarioOf(deviceTimeZoneId: String) =
                scenarioOf(deviceTimeZoneId, "2021-03-28T06:00:00+02:00", startedHoursAgo = 7, expectedScrollAmount = 2856)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: device = {0}, sessionStartsAt = {1}, conferenceStartedHoursAgo = {2} -> scrollAmount = {3}")
        fun data() = timeZoneOffsets.map { startsNowScenarioOf(deviceTimeZoneId = "GMT$it") } +
                timeZoneOffsets.map { startedBeforeScenarioOf(deviceTimeZoneId = "GMT$it") } +
                timeZoneOffsets.map { winterSummerScenarioOf(deviceTimeZoneId = "GMT$it") }

    }

    @Test
    fun calculateScrollAmount() {
        withTimeZone(deviceTimeZoneId) {
            val targetSessionStartsAtDateTime = DateParser.getDateTime(sessionStartsAtDateTimeIso8601)
            val targetSessionStartsAt = Moment.ofEpochMilli(targetSessionStartsAtDateTime)
            val targetSession = createBaseSession(sessionId = "target", moment = targetSessionStartsAt)

            val sessions = if (conferenceStartedHoursAgo == 0) {
                listOf(targetSession)
            } else {
                val firstSessionStartsAt = targetSessionStartsAt.minusHours(conferenceStartedHoursAgo.toLong())
                val firstSession = createBaseSession(sessionId = "first", moment = firstSessionStartsAt)
                listOf(firstSession, targetSession)
            }

            val conference = Conference.ofSessions(sessions)
            val scrollAmount = ScrollAmountCalculator(NoLogging).calculateScrollAmount(conference, targetSession, BOX_HEIGHT)
            assertThat(scrollAmount).isEqualTo(expectedScrollAmount)
        }
    }

    private fun createBaseSession(sessionId: String, moment: Moment) = Session(sessionId).apply {
        day = 0
        date = moment.toZonedDateTime(ZoneOffset.UTC).toLocalDate().toString()
        dateUTC = moment.toMilliseconds()
        startTime = moment.minuteOfDay
        relStartTime = moment.minuteOfDay // This might now always be the case, see ParserTask.parseFahrplan
        duration = 60
        room = "Main hall"
    }

}
