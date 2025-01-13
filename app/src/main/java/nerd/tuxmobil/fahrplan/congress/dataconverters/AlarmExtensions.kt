@file:JvmName("AlarmExtensions")

package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import info.metadude.android.eventfahrplan.database.models.Alarm as DatabaseAlarm

fun Alarm.toAlarmDatabaseModel() = DatabaseAlarm(
        id = id,
        alarmTimeInMin = alarmTimeInMin,
        day = day,
        displayTime = displayTime,
        guid = guid,
        title = sessionTitle,
        time = startTime,
        timeText = timeText
)

fun Alarm.toSchedulableAlarm() = SchedulableAlarm(
        day = day,
        guid = guid,
        sessionTitle = sessionTitle,
        startTime = startTime
)

fun DatabaseAlarm.toAlarmAppModel() = Alarm(
        id = id,
        alarmTimeInMin = alarmTimeInMin,
        day = day,
        displayTime = displayTime,
        guid = guid,
        sessionTitle = title,
        startTime = time,
        timeText = timeText
)
