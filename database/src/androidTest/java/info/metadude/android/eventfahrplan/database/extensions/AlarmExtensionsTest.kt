package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.models.Alarm
import org.junit.jupiter.api.Test

class AlarmExtensionsTest {

    @Test
    fun toContentValues() {
        val alarm = Alarm(
            dayIndex = 4,
            sessionId = "5237",
            time = Moment.ofEpochMilli(1509617700001L),
            title = "My title",
        )
        val values = alarm.toContentValues()
        assertThat(values.getAsInteger(DAY_INDEX)).isEqualTo(4)
        assertThat(values.getAsString(SESSION_ID)).isEqualTo("5237")
        assertThat(values.getAsLong(TIME)).isEqualTo(1509617700001L)
        assertThat(values.getAsString(SESSION_TITLE)).isEqualTo("My title")
    }

}
