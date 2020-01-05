package nerd.tuxmobil.fahrplan.congress.exceptions

import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlin.coroutines.CoroutineContext

class AppExceptionHandler(

        val logging: Logging

) : ExceptionHandling {

    override fun onExceptionHandling(context: CoroutineContext, throwable: Throwable) {
        logging.e(context.toString(), throwable.message ?: "")
        throwable.printStackTrace()
    }

}
