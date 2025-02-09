package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface ExecutionContext {

    val ui: CoroutineContext
    val network: CoroutineContext
    val database: CoroutineContext

    suspend fun <T> withUiContext(block: suspend CoroutineScope.() -> T) =
            withContext(context = ui, block = block)

}
