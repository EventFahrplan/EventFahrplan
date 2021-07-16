package nerd.tuxmobil.fahrplan.congress.sharing

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.core.app.ShareCompat

object SessionSharer {
    // String formattedSessions can be one or multiple sessions
    @JvmStatic
    fun shareSimple(context: Context, formattedSessions: String) {
        // Show system's share UI. It handles the case of no matching apps.
        val intent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(formattedSessions)
            .createChooserIntent()
        context.startActivity(intent)
    }

    @JvmStatic
    fun shareJson(context: Context, formattedSessions: String): Boolean {
        val intent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(formattedSessions)
            .intent

        // Don't use the system's share UI so Chaosflix can be selected as default app
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
