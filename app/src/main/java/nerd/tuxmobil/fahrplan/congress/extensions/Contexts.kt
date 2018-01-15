@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE

fun Context.getAlarmManager() = getSystemService(ALARM_SERVICE) as AlarmManager
