@file:JvmName("AlarmExtensions")

package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import info.metadude.android.eventfahrplan.database.models.Alarm as DatabaseAlarm

fun Alarm.toAlarmDatabaseModel() = DatabaseAlarm(
    id = id,
    dayIndex = dayIndex,
    sessionId = sessionId,
    title = sessionTitle,
    firesAt = firesAt,
)

fun Alarm.toSchedulableAlarm() = SchedulableAlarm(
    dayIndex = dayIndex,
    sessionId = sessionId,
    sessionTitle = sessionTitle,
    firesAt = firesAt,
)

fun DatabaseAlarm.toAlarmAppModel() = Alarm(
    id = id,
    dayIndex = dayIndex,
    sessionId = sessionId,
    sessionTitle = title,
    firesAt = firesAt,
)
