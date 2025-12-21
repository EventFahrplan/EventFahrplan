package nerd.tuxmobil.fahrplan.congress.reporting

import android.app.Activity
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import de.cketti.mailto.EmailIntentBuilder
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import org.ligi.tracedroid.TraceDroid
import org.ligi.tracedroid.collecting.TraceDroidMetaInfo

// This class supports translation and configuration via XML files.
// The original TraceDroidEmailSender class is available here:
// https://github.com/ligi/tracedroid
object TraceDroidEmailSender {

    fun sendStackTraces(context: Activity) {
        if (TraceDroid.getStackTraceFiles().isNullOrEmpty()) {
            return
        }

        val appName = context.getString(R.string.app_name)
        val emailAddress = BuildConfig.TRACE_DROID_EMAIL_ADDRESS
        val dialogTitle = context.getString(
            R.string.trace_droid_dialog_title, appName
        )
        val dialogMessage = context.getString(
            R.string.trace_droid_dialog_message, appName
        )
        val buttonTitleSend = context.getString(
            R.string.trace_droid_button_title_send
        )
        val buttonTitleLater = context.getString(
            R.string.trace_droid_button_title_later
        )
        val buttonTitleNo = context.getString(
            R.string.trace_droid_button_title_no
        )
        val maximumStackTracesCount = context.resources.getInteger(
            R.integer.config_trace_droid_maximum_stack_traces_count
        )

        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(buttonTitleSend) { _, _ ->
                val emailIntent = EmailIntentBuilder
                    .from(context)
                    .to(emailAddress)
                    .subject("[TraceDroid Report] ${TraceDroidMetaInfo.getAppPackageName()}")
                    .body(TraceDroid.getStackTraceText(maximumStackTracesCount))
                    .build()

                try {
                    context.startActivity(emailIntent)
                    TraceDroid.deleteStacktraceFiles()
                } catch (_: ActivityNotFoundException) {
                    val message = context.getString(R.string.trace_droid_no_email_app)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(buttonTitleNo) { _, _ -> TraceDroid.deleteStacktraceFiles() }
            .setNeutralButton(buttonTitleLater) { _, _ -> }
            .show()
    }

}
