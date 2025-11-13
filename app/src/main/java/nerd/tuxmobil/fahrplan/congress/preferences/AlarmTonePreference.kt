package nerd.tuxmobil.fahrplan.congress.preferences

import android.net.Uri
import android.provider.Settings

// TODO: Move DEFAULT_VALUE_STRING to where the other default values are stored and remove this file.
class AlarmTonePreference {

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

}
