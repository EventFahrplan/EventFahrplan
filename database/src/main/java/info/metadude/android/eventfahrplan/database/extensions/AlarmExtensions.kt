package info.metadude.android.eventfahrplan.database.extensions

import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.models.Alarm

fun Alarm.toContentValues() = contentValuesOf(
        DAY_INDEX to dayIndex,
        SESSION_ID to sessionId,
        SESSION_TITLE to title,
        TIME to time.toMilliseconds(),
)
