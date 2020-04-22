package nerd.tuxmobil.fahrplan.congress.errormessaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus

/**
 * Send and receive internal error messages via this class.
 * Only send error messages which should be displayed to the user.
 *
 * [LocalBroadcastManager] is used internally for the message transport.
 */
internal class LocalErrorMessaging(

        val context: Context,
        val onErrorMessageReceived: (ErrorMessage) -> Unit

) {

    companion object {

        private const val INTENT_ACTION_ERROR_MESSAGING = "ERROR_MESSAGING"
        const val INTENT_EXTRA_EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE"
        const val INTENT_EXTRA_HTTP_STATUS = "HTTP_STATUS"
        const val INTENT_EXTRA_HOST_NAME = "HOST_NAME"
        const val UNKNOWN_HTTP_STATUS = -1

        /**
         * Sends the given [errorMessage].
         */
        fun sendErrorMessage(context: Context, errorMessage: ErrorMessage) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                    Intent(INTENT_ACTION_ERROR_MESSAGING).applyErrorMessage { errorMessage }
            )
        }
    }

    /**
     * Receives [error messages][ErrorMessage] and forwards them to [onErrorMessageReceived].
     */
    private val errorMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { onErrorMessageReceived(it.toErrorMessage()) }
        }
    }

    /**
     * Starts to receive error messages - to be called in [android.app.Activity.onStart].
     */
    fun startReceiving() = LocalBroadcastManager.getInstance(context.applicationContext)
            .registerReceiver(errorMessageReceiver, IntentFilter(INTENT_ACTION_ERROR_MESSAGING))

    /**
     * Stops to receive error messages - to be called in [android.app.Activity.onStop].
     */
    fun stopReceiving() = LocalBroadcastManager.getInstance(context.applicationContext)
            .unregisterReceiver(errorMessageReceiver)

    private fun Intent.toErrorMessage() = ErrorMessage(
            HttpStatus.of(getIntExtra(INTENT_EXTRA_HTTP_STATUS, UNKNOWN_HTTP_STATUS)),
            getStringExtra(INTENT_EXTRA_EXCEPTION_MESSAGE),
            getStringExtra(INTENT_EXTRA_HOST_NAME)
    )

}

private inline fun Intent.applyErrorMessage(getErrorMessage: () -> ErrorMessage) = apply {
    val errorMessage = getErrorMessage()
    putExtra(LocalErrorMessaging.INTENT_EXTRA_HTTP_STATUS, errorMessage.httpStatus.ordinal)
    putExtra(LocalErrorMessaging.INTENT_EXTRA_EXCEPTION_MESSAGE, errorMessage.exceptionMessage)
    putExtra(LocalErrorMessaging.INTENT_EXTRA_HOST_NAME, errorMessage.hostName)
}
