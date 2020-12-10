package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Alarm as AlarmDatabaseModel

class AlarmExtensionsTest {

    @Test
    fun toAlarmAppModel_toAlarmDatabaseModel() {
        val alarm = AlarmDatabaseModel(
                alarmTimeInMin = 20,
                day = 4,
                displayTime = 1509617700000L,
                sessionId = "11111111-1111-1111-1111-111111111111",
                time = 1509617700001L,
                timeText = "02/11/2017 11:05",
                title = "My title"
        )
        assertThat(alarm.toAlarmAppModel().toAlarmDatabaseModel()).isEqualTo(alarm)
    }

    @Test
    fun toSchedulableAlarm() {
        val alarm = Alarm(
                alarmTimeInMin = 20,
                day = 4,
                displayTime = 1509617700000L,
                sessionId = "11111111-1111-1111-1111-111111111111",
                sessionTitle = "My title",
                startTime = 1509617700001L,
                timeText = "02/11/2017 11:05"
        )
        val schedulableAlarm = SchedulableAlarm(
                day = 4,
                sessionId = "11111111-1111-1111-1111-111111111111",
                sessionTitle = "My title",
                startTime = 1509617700001L
        )
        assertThat(alarm.toSchedulableAlarm()).isEqualTo(schedulableAlarm)
    }


}
