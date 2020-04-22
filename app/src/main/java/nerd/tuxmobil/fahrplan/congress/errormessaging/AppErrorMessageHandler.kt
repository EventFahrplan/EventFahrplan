package nerd.tuxmobil.fahrplan.congress.errormessaging

import android.content.Context

class AppErrorMessageHandler @JvmOverloads constructor(

        val context: Context,
        private val onSendErrorMessage: (Context, ErrorMessage) -> Unit =
                LocalErrorMessaging.Companion::sendErrorMessage


) : ErrorMessageHandling {

    override fun onHandleErrorMessage(errorMessage: ErrorMessage) {
        onSendErrorMessage(context, errorMessage)
    }

}
