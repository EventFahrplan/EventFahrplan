@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.*
import android.net.ConnectivityManager
import android.view.LayoutInflater

fun Context.getAlarmManager() = getSystemService(ALARM_SERVICE) as AlarmManager

fun Context.getConnectivityManager() = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.getLayoutInflater() = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

fun Context.getNotificationManager() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
