package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
object TestExecutionContext : ExecutionContext {

    override val ui: CoroutineDispatcher = Unconfined
    override val network: CoroutineDispatcher = Unconfined
    override val database: CoroutineDispatcher = Unconfined

}
