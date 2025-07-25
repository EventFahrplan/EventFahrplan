package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import org.junit.jupiter.api.Test
import info.metadude.android.eventfahrplan.database.models.Alarm as AlarmDatabaseModel

class AlarmExtensionsTest {

    @Test
    fun toAlarmAppModel_toAlarmDatabaseModel() {
        val alarm = AlarmDatabaseModel(
            dayIndex = 4,
            sessionId = "5237",
            time = Moment.ofEpochMilli(1509617700001L),
            title = "My title",
        )
        assertThat(alarm.toAlarmAppModel().toAlarmDatabaseModel()).isEqualTo(alarm)
    }

    @Test
    fun toSchedulableAlarm() {
        val alarm = Alarm(
            dayIndex = 4,
            sessionId = "5237",
            sessionTitle = "My title",
            startTime = Moment.ofEpochMilli(1509617700001L),
        )
        val schedulableAlarm = SchedulableAlarm(
            dayIndex = 4,
            sessionId = "5237",
            sessionTitle = "My title",
            startTime = Moment.ofEpochMilli(1509617700001L),
        )
        assertThat(alarm.toSchedulableAlarm()).isEqualTo(schedulableAlarm)
    }


}
