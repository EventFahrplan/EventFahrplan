package nerd.tuxmobil.fahrplan.congress.sharing

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

object SessionSharer {
    // String formattedSessions can be one or multiple sessions
    @JvmStatic
    fun shareSimple(context: Context, formattedSessions: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formattedSessions)
        }

        // Show system's share UI. It handles the case of no matching apps.
        val chooserIntent = Intent.createChooser(intent, null)
        context.startActivity(chooserIntent)
    }

    @JvmStatic
    fun shareJson(context: Context, formattedSessions: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/json"
            putExtra(Intent.EXTRA_TEXT, formattedSessions)
        }

        // Don't use the system's share UI so Chaosflix can be selected as default app
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
