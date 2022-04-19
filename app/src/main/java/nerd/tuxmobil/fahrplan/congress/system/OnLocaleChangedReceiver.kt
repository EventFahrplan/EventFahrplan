package nerd.tuxmobil.fahrplan.congress.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper

/**
 * Handler for androids [Intent.ACTION_LOCALE_CHANGED] event.
 * Recreates notification channel settings using [NotificationHelper].
 */
class OnLocaleChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_LOCALE_CHANGED) {
            return
        }

        val notificationHelper = NotificationHelper(context)
        notificationHelper.createChannels()
    }
}