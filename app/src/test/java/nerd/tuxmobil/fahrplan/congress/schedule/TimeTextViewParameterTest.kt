package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.annotation.LayoutRes
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.dataconverters.toStartsAtMoment
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneOffset
import java.util.Locale
import java.util.TimeZone

class TimeTextViewParameterTest {

    private companion object {
        const val NORMALIZED_BOX_HEIGHT = 34 // Pixel 2 portrait mode
    }

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @After
    fun resetSystemDefaults() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun `parametersOf returns four view parameters without -now- view parameter`() {
        val moment = Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT)
        val nowMoment = createSession(moment).toStartsAtMoment()
        val dayIndex = 2 // represents tomorrow
        val parameters = parametersOf(nowMoment, moment, dayIndex)
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert(R.layout.time_layout, "08:00")
        parameters[1].assert(R.layout.time_layout, "08:15")
        parameters[2].assert(R.layout.time_layout, "08:30")
        parameters[3].assert(R.layout.time_layout, "08:45")
    }

    @Test
    fun `parametersOf returns four view parameters including one -now- view parameter`() {
        val moment = Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT)
        val nowMoment = createSession(moment).toStartsAtMoment().plusMinutes(30)
        val dayIndex = 1 // represents today
        val parameters = parametersOf(nowMoment, moment, dayIndex)
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert(R.layout.time_layout, "08:00")
        parameters[1].assert(R.layout.time_layout, "08:15")
        parameters[2].assert(R.layout.time_layout_now, "08:30")
        parameters[3].assert(R.layout.time_layout, "08:45")
    }

    @Test
    fun `parametersOf returns four view parameters for a session crossing the intra-day limit`() {
        val moment = Moment.ofEpochMilli(1583019000000L) // February 29, 2020 11:30:00 PM GMT
        val nowMoment = createSession(moment).toStartsAtMoment()
        val dayIndex = 2 // represents tomorrow
        val parameters = parametersOf(nowMoment, moment, dayIndex)
        assertThat(parameters.size).isEqualTo(4)
        parameters[0].assert(R.layout.time_layout, "23:30")
        parameters[1].assert(R.layout.time_layout, "23:45")
        parameters[2].assert(R.layout.time_layout, "00:00")
        parameters[3].assert(R.layout.time_layout, "00:15")
    }

    private fun parametersOf(nowMoment: Moment, moment: Moment, dayIndex: Int): List<TimeTextViewParameter> {
        val session = createSession(moment)
        val conference = Conference.ofSessions(listOf(session))
        return TimeTextViewParameter.parametersOf(nowMoment, conference, moment.monthDay, dayIndex, NORMALIZED_BOX_HEIGHT)
    }

    private fun TimeTextViewParameter.assert(@LayoutRes layout: Int, titleText: String) {
        assertThat(this.layout).isEqualTo(layout)
        assertThat(this.height).isEqualTo(102)
        assertThat(this.titleText).isEqualTo(titleText)
    }

    private fun createSession(moment: Moment) = Session("s1").apply {
        day = 0
        date = moment.toZonedDateTime(ZoneOffset.UTC).toLocalDate().toString()
        dateUTC = moment.toMilliseconds()
        startTime = moment.minuteOfDay
        duration = 60
        room = "Main hall"
    }

}
