package nerd.tuxmobil.fahrplan.congress.exceptions

import nerd.tuxmobil.fahrplan.congress.logging.Logging
import kotlin.coroutines.CoroutineContext

class AppExceptionHandler(

        val logging: Logging

) : ExceptionHandling {

    override fun onExceptionHandling(context: CoroutineContext, throwable: Throwable) {
        logging.e(context.toString(), throwable.message ?: "")
        throwable.printStackTrace()
    }

}
