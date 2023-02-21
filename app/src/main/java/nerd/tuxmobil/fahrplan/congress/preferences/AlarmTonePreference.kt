package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import nerd.tuxmobil.fahrplan.congress.extensions.getParcelableExtraCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion

/**
 * Represents an alarm tone preference.
 */
class AlarmTonePreference : Preference {

    companion object {

        /**
         * See [Settings.System.DEFAULT_ALARM_ALERT_URI]
         */
        val DEFAULT_VALUE_URI: Uri = Settings.System.DEFAULT_ALARM_ALERT_URI

        /**
         * Content `Uri` string representation of [DEFAULT_VALUE_URI].
         */
        val DEFAULT_VALUE_STRING: String = DEFAULT_VALUE_URI.toString()

    }

    @Suppress("unused")
    constructor(context: Context) :
            super(context) {
        applyDefaultValue()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs) {
        applyDefaultValue()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        applyDefaultValue()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        applyDefaultValue()
    }

    private fun applyDefaultValue() {
        setDefaultValue(DEFAULT_VALUE_STRING)
    }

    override fun onClick() {
        preferenceManager.showDialog(this)
    }

    fun showAlarmTonePicker(fragment: Fragment, requestCode: Int) {
        val persistedString: String = getPersistedString(DEFAULT_VALUE_STRING)
        val alarmToneUri = AlarmToneConversion.getPickerIntentUri(persistedString)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).withExtras(
                RingtoneManager.EXTRA_RINGTONE_TYPE to RingtoneManager.TYPE_ALARM,
                RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT to true,
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT to true,
                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI to DEFAULT_VALUE_URI,
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI to alarmToneUri
        )
        fragment.startActivityForResult(intent, requestCode)
    }

    fun onAlarmTonePicked(intent: Intent) {
        val alarmToneUri = intent.getParcelableExtraCompat<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        persistString(AlarmToneConversion.getPersistableString(alarmToneUri))
    }

}
