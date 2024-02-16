package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.ALARM_TIME_IN_MIN
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DISPLAY_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME_TEXT
import info.metadude.android.eventfahrplan.database.models.Alarm
import org.junit.jupiter.api.Test

class AlarmExtensionsTest {

    @Test
    fun toContentValues() {
        val alarm = Alarm(
                alarmTimeInMin = 20,
                day = 4,
                displayTime = 1509617700000L,
                sessionId = "5237",
                time = 1509617700001L,
                timeText = "02/11/2017 11:05",
                title = "My title"
        )
        val values = alarm.toContentValues()
        assertThat(values.getAsInteger(ALARM_TIME_IN_MIN)).isEqualTo(20)
        assertThat(values.getAsInteger(DAY)).isEqualTo(4)
        assertThat(values.getAsLong(DISPLAY_TIME)).isEqualTo(1509617700000L)
        assertThat(values.getAsString(SESSION_ID)).isEqualTo("5237")
        assertThat(values.getAsLong(TIME)).isEqualTo(1509617700001L)
        assertThat(values.getAsString(TIME_TEXT)).isEqualTo("02/11/2017 11:05")
        assertThat(values.getAsString(SESSION_TITLE)).isEqualTo("My title")
    }

}
