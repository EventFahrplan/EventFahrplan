package nerd.tuxmobil.fahrplan.congress

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Unconfined
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

// TODO Replace Unconfined with EmptyCoroutineContext? Ref: https://code.cash.app/dispatchers-unconfined
object TestExecutionContext : ExecutionContext {
    override val ui: CoroutineDispatcher = Unconfined
    override val network: CoroutineDispatcher = Unconfined
    override val database: CoroutineDispatcher = Unconfined
}
