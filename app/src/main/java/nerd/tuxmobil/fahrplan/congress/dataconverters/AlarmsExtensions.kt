package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.Alarm as DatabaseAlarm


fun List<DatabaseAlarm>.toAlarmsAppModel() = map(DatabaseAlarm::toAlarmAppModel)
