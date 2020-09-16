package nerd.tuxmobil.fahrplan.congress.utils

import android.net.Uri
import androidx.core.net.toUri

/**
 * Conversions (`Uri` <-> `String`) related to alarm tone data.
 */
internal object AlarmToneConversion {

    private val SILENT_URI: Uri? = null
    private const val SILENT_STRING: String = ""

    /**
     * Returns a nullable `Uri` to configure a notification with.
     */
    fun getNotificationIntentUri(alarmTone: String?, defaultValue: Uri): Uri? = when (alarmTone) {
        null -> defaultValue
        SILENT_STRING -> SILENT_URI
        else -> alarmTone.toUri()
    }

    /**
     * Returns a nullable `String` to be persisted.
     */
    fun getPersistableString(alarmToneUri: Uri?): String? {
        return alarmToneUri?.toString() ?: SILENT_STRING
    }

    /**
     * Returns a nullable `Uri` to initialize an alarm tone picker with.
     */
    fun getPickerIntentUri(persistedAlarmTone: String): Uri? {
        return if (persistedAlarmTone == SILENT_STRING) SILENT_URI else persistedAlarmTone.toUri()
    }

}
