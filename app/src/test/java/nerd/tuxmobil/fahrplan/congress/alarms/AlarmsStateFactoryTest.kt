package nerd.tuxmobil.fahrplan.congress.alarms

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.threeten.bp.ZoneOffset

class AlarmsStateFactoryTest {

    private companion object {
        val SESSION_STARTS_AT = Moment.ofEpochMilli(1683981000000) // 2023-05-13T12:30:00+00:00
    }

    @Nested
    inner class Empty {

        @Test
        fun `createAlarmsState returns empty list when alarms is empty`() {
            val factory = createAlarmsStateFactory(0)

            val alarms = emptyList<Alarm>()
            val sessions = listOf(Session("s0"))
            val useDeviceTimeZone = false

            val expected = emptyList<SessionAlarmParameter>()
            assertThat(factory.createAlarmsState(alarms, sessions, useDeviceTimeZone)).isEqualTo(expected)
        }

        @Test
        fun `createAlarmsState returns empty list when sessions is empty`() {
            val factory = createAlarmsStateFactory(0)

            val alarms = listOf(createAlarm("s0"))
            val sessions = emptyList<Session>()
            val useDeviceTimeZone = false

            val expected = emptyList<SessionAlarmParameter>()
            assertThat(factory.createAlarmsState(alarms, sessions, useDeviceTimeZone)).isEqualTo(expected)
        }

        @Test
        fun `createAlarmsState returns empty list when alarm and session are not associated`() {
            val factory = createAlarmsStateFactory(0)

            val alarms = listOf(createAlarm("s1"))
            val sessions = listOf(Session("s0"))
            val useDeviceTimeZone = false

            val expected = emptyList<SessionAlarmParameter>()
            assertThat(factory.createAlarmsState(alarms, sessions, useDeviceTimeZone)).isEqualTo(expected)
        }

    }

    @Nested
    inner class Values {

        @Test
        fun `createAlarmsState returns values list when alarm and session are associated, 10 min`() {
            val alarmTimeInMin = 10
            val alarmStartsAt = calculateAlarmStartsAt(alarmTimeInMin).toMilliseconds()
            val factory = createAlarmsStateFactory(alarmTimeInMin)

            val alarms = listOf(
                createAlarm(
                    sessionId = "s0",
                    alarmTimeInMin = alarmTimeInMin,
                    alarmStartsAt = alarmStartsAt,
                )
            )
            val sessions = listOf(
                Session(
                    sessionId = "s0",
                    title = "Title",
                    subtitle = "Subtitle",
                    dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                    timeZoneOffset = ZoneOffset.of("+00:00"),
                )
            )
            val useDeviceTimeZone = false

            val expected = listOf(
                SessionAlarmParameter(
                    sessionId = "s0",
                    title = "Title",
                    titleContentDescription = "Title: Berlin stories",
                    subtitle = "Subtitle",
                    subtitleContentDescription = "Subtitle: News from Berlin",
                    alarmOffsetContentDescription = "Alarm: 10 minutes before",
                    alarmOffsetInMin = alarmTimeInMin,
                    firesAt = alarmStartsAt,
                    firesAtText = "May 13, 2023, 12:20 PM",
                    firesAtContentDescription = "Fires at: omitted in this test",
                    dayIndex = 0,
                    selected = false,
                )
            )
            assertThat(factory.createAlarmsState(alarms, sessions, useDeviceTimeZone)).isEqualTo(expected)
        }

        @Test
        fun `createAlarmsState returns values list when alarm and session are associated, 0 min`() {
            val alarmTimeInMin = 0
            val alarmStartsAt = calculateAlarmStartsAt(alarmTimeInMin).toMilliseconds()
            val factory = createAlarmsStateFactory(alarmTimeInMin)

            val alarms = listOf(
                createAlarm(
                    sessionId = "s0",
                    alarmTimeInMin = alarmTimeInMin,
                    alarmStartsAt = alarmStartsAt,
                )
            )
            val sessions = listOf(
                Session(
                    sessionId = "s0",
                    title = "Title",
                    subtitle = "Subtitle",
                    dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                    timeZoneOffset = ZoneOffset.of("+00:00"),
                )
            )
            val useDeviceTimeZone = false

            val expected = listOf(
                SessionAlarmParameter(
                    sessionId = "s0",
                    title = "Title",
                    titleContentDescription = "Title: Berlin stories",
                    subtitle = "Subtitle",
                    subtitleContentDescription = "Subtitle: News from Berlin",
                    alarmOffsetContentDescription = "Alarm: at start time",
                    alarmOffsetInMin = alarmTimeInMin,
                    firesAt = alarmStartsAt,
                    firesAtText = "May 13, 2023, 12:30 PM",
                    firesAtContentDescription = "Fires at: omitted in this test",
                    dayIndex = 0,
                    selected = false,
                )
            )
            assertThat(factory.createAlarmsState(alarms, sessions, useDeviceTimeZone)).isEqualTo(expected)
        }

    }

    private fun createAlarmsStateFactory(alarmTimeInMin: Int) =
        AlarmsStateFactory(CompleteResourceResolver(alarmTimeInMin), DateFormatterDelegate)

    private fun createAlarm(
        sessionId: String,
        alarmTimeInMin: Int = 10,
        alarmStartsAt: Long = 1620909000000,
    ) = Alarm(
        alarmTimeInMin = alarmTimeInMin,
        day = 2,
        displayTime = -1,
        sessionId = sessionId,
        sessionTitle = "Unused",
        startTime = alarmStartsAt,
        timeText = "Unused",
    )

    private fun calculateAlarmStartsAt(alarmTimeInMin: Int) =
        SESSION_STARTS_AT.minusMinutes(alarmTimeInMin.toLong())

}

private class CompleteResourceResolver(val alarmTimeInMin: Int) : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.session_list_item_title_content_description -> "Title: Berlin stories"
        R.string.session_list_item_subtitle_content_description -> "Subtitle: News from Berlin"
        R.string.alarms_item_alarm_time_zero_minutes_content_description -> "Alarm: at start time"
        R.string.alarms_item_alarm_time_minutes_content_description -> "Alarm: $alarmTimeInMin minutes before"
        R.string.alarms_item_fires_at_content_description -> "Fires at: omitted in this test"
        else -> fail("Unknown string id : $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        throw NotImplementedError("Not needed for this test.")
    }
}
