package nerd.tuxmobil.fahrplan.congress.settings

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager.ACTION_RINGTONE_PICKER
import android.media.RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI
import android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI
import android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI
import android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT
import android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT
import android.media.RingtoneManager.EXTRA_RINGTONE_TYPE
import android.media.RingtoneManager.TYPE_ALARM
import android.net.Uri
import android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
import androidx.activity.result.contract.ActivityResultContract
import nerd.tuxmobil.fahrplan.congress.extensions.getParcelableExtraCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.settings.AlarmToneResult.AlarmToneUri
import nerd.tuxmobil.fahrplan.congress.settings.AlarmToneResult.Canceled

internal class PickAlarmToneContract : ActivityResultContract<Uri?, AlarmToneResult>() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return Intent(ACTION_RINGTONE_PICKER).withExtras(
            EXTRA_RINGTONE_TYPE to TYPE_ALARM,
            EXTRA_RINGTONE_SHOW_DEFAULT to true,
            EXTRA_RINGTONE_SHOW_SILENT to true,
            EXTRA_RINGTONE_DEFAULT_URI to DEFAULT_ALARM_ALERT_URI,
            EXTRA_RINGTONE_EXISTING_URI to input,
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AlarmToneResult {
        return if (resultCode == RESULT_OK && intent != null) {
            AlarmToneUri(intent.getParcelableExtraCompat<Uri>(EXTRA_RINGTONE_PICKED_URI))
        } else {
            Canceled
        }
    }
}

internal sealed interface AlarmToneResult {
    data object Canceled : AlarmToneResult
    data class AlarmToneUri(val alarmToneUri: Uri?) : AlarmToneResult
}
