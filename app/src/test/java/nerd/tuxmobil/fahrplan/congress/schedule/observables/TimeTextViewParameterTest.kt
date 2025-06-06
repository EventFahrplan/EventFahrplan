package nerd.tuxmobil.fahrplan.congress.schedule.observables

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.schedule.Conference
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset
import java.util.Locale
import java.util.TimeZone

class TimeTextViewParameterTest {

    private companion object {
        const val NORMALIZED_BOX_HEIGHT = 34 // Pixel 2 portrait mode
    }

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()

    @BeforeEach
    fun setUp() {
        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @AfterEach
    fun resetSystemDefaults() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun `parametersOf returns four view parameters marked isNow=false if the session happened yesterday`() {
        val moment = Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT
        val nowMoment = moment.plusDays(1)
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(60))
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert("08:00", isNow = false)
        parameters[1].assert("08:15", isNow = false)
        parameters[2].assert("08:30", isNow = false)
        parameters[3].assert("08:45", isNow = false)
    }

    @Test
    fun `parametersOf returns four view parameters marked isNow=false if the session happened at the same date last month`() {
        val momentInFebruary = Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT
        val momentInMarch = Moment.ofEpochMilli(1585468800000L) // March 29, 2020 08:00:00 AM GMT
        val nowMoment = momentInMarch.plusMinutes(30)
        val parameters = parametersOf(nowMoment, momentInFebruary, Duration.ofMinutes(60))
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert("08:00", isNow = false)
        parameters[1].assert("08:15", isNow = false)
        parameters[2].assert("08:30", isNow = false)
        parameters[3].assert("08:45", isNow = false)
    }

    @Test
    fun `parametersOf returns four view parameters including one marked isNow=true if the session happens now`() {
        val moment = Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT
        val nowMoment = moment.plusMinutes(30)
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(60))
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert("08:00", isNow = false)
        parameters[1].assert("08:15", isNow = false)
        parameters[2].assert("08:30", isNow = true)
        parameters[3].assert("08:45", isNow = false)
    }

    @Test
    fun `parametersOf returns four view parameters for a session crossing the intra-day limit if the session happened yesterday`() {
        val moment = Moment.ofEpochMilli(1583019000000L) // February 29, 2020 11:30:00 PM GMT
        val nowMoment = moment.plusDays(1)
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(60))
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert("23:30", isNow = false)
        parameters[1].assert("23:45", isNow = false)
        parameters[2].assert("00:00", isNow = false)
        parameters[3].assert("00:15", isNow = false)
    }

    @Test
    fun `parametersOf returns four view parameters including one marked isNow=true for a session crossing the intra-day limit`() {
        val moment = Moment.ofEpochMilli(1583019000000L) // February 29, 2020 11:30:00 PM GMT
        val nowMoment = moment.plusMinutes(45) // March 1, 2020 00:15:00 AM GMT
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(60))
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert("23:30", isNow = false)
        parameters[1].assert("23:45", isNow = false)
        parameters[2].assert("00:00", isNow = false)
        parameters[3].assert("00:15", isNow = true)
    }

    @Test
    fun `parametersOf returns 20 view parameters for a session crossing the daylight saving time start`() {
        val moment = Moment.ofEpochMilli(1616889600000L) // March 28, 2021 12:00:00 AM GMT
        val nowMoment = moment.plusDays(1) // March 29, 2021 12:00:00 AM GMT
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(300))
        assertThat(parameters.size).isEqualTo(20)
        parameters[0].assert("00:00", isNow = false)
        parameters[2].assert("00:30", isNow = false)
        parameters[4].assert("01:00", isNow = false)
        parameters[6].assert("01:30", isNow = false)
        parameters[8].assert("02:00", isNow = false) // Clock turns to 03:00 summer time, currently not supported, see Conference class
        parameters[10].assert("02:30", isNow = false)
        parameters[12].assert("03:00", isNow = false)
        parameters[14].assert("03:30", isNow = false)
        parameters[16].assert("04:00", isNow = false)
    }

    @Test
    fun `parametersOf returns 20 view parameters for a session crossing the daylight saving time end`() {
        val moment = Moment.ofEpochMilli(1635638400000L) // October 31, 2021 12:00:00 AM GMT
        val nowMoment = moment.plusDays(1) // November 1, 2021 12:00:00 AM GMT
        val parameters = parametersOf(nowMoment, moment, Duration.ofMinutes(300))
        assertThat(parameters.size).isEqualTo(20)
        parameters[0].assert("00:00", isNow = false)
        parameters[2].assert("00:30", isNow = false)
        parameters[4].assert("01:00", isNow = false)
        parameters[6].assert("01:30", isNow = false)
        parameters[8].assert("02:00", isNow = false)
        parameters[10].assert("02:30", isNow = false)
        parameters[12].assert("03:00", isNow = false) // Clock turns to 02:00 winter time, currently not supported, see Conference class
        parameters[14].assert("03:30", isNow = false)
        parameters[16].assert("04:00", isNow = false)
    }

    private fun parametersOf(nowMoment: Moment, moment: Moment, duration: Duration): List<TimeTextViewParameter> {
        val session = createSession(moment, duration)
        val conference = Conference.ofSessions(listOf(session))
        return TimeTextViewParameter.parametersOf(nowMoment, conference, NORMALIZED_BOX_HEIGHT, useDeviceTimeZone = false)
    }

    private fun TimeTextViewParameter.assert(titleText: String, isNow: Boolean) {
        assertThat(this.height).isEqualTo(102)
        assertThat(this.titleText).isEqualTo(titleText)
        assertThat(this.isNow).isEqualTo(isNow)
    }

    private fun createSession(moment: Moment, duration: Duration = Duration.ofMinutes(60)) = Session(
        sessionId = "s1",
        dayIndex = 0,
        dateText = moment.toZonedDateTime(ZoneOffset.UTC).toLocalDate().toString(),
        dateUTC = moment.toMilliseconds(),
        startTime = Duration.ofMinutes(moment.minuteOfDay),
        duration = duration,
        roomName = "Main hall",
    )

}