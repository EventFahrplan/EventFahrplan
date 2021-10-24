package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.exceptions.ExceptionHandling

class DatabaseScope private constructor(

    private val executionContext: ExecutionContext,
    parentJob: Job,
    exceptionHandler: CoroutineExceptionHandler

) {

    companion object {

        @JvmStatic
        fun of(executionContext: ExecutionContext, exceptionHandling: ExceptionHandling): DatabaseScope {
            val defaultExceptionHandler = CoroutineExceptionHandler(exceptionHandling::onExceptionHandling)
            return DatabaseScope(executionContext, SupervisorJob(), defaultExceptionHandler)
        }

    }

    private val scope = CoroutineScope(executionContext.database + parentJob + exceptionHandler)

    fun launchNamed(name: String, block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(context = CoroutineName(name), block = block)
    }

    suspend fun <T> withUiContext(block: suspend CoroutineScope.() -> T) =
        executionContext.withUiContext(block)

}
