package nerd.tuxmobil.fahrplan.congress.exceptions

import kotlin.coroutines.CoroutineContext

interface ExceptionHandling {

    fun onExceptionHandling(context: CoroutineContext, throwable: Throwable)

}
