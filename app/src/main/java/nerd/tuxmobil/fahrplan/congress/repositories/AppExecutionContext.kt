package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext

object AppExecutionContext : ExecutionContext {

    override val ui: CoroutineContext = Main
    override val network: CoroutineContext = IO
    override val database: CoroutineContext = IO

}
