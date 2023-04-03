package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

class ChangeListViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging

) : ViewModel() {

    private companion object {
        const val LOG_TAG = "ChangesListViewModel"
    }

    val changeListParameter: Flow<ChangeListParameter> = repository.changedSessions
        .map { sessions -> sessions.toChangeListParameter() }
        .flowOn(executionContext.database)

    private val mutableScheduleChangesSeen = Channel<Unit>()
    val scheduleChangesSeen = mutableScheduleChangesSeen.receiveAsFlow()

    fun updateScheduleChangesSeen(changesSeen: Boolean) {
        launch {
            mutableScheduleChangesSeen.sendOneTimeEvent(Unit)
            repository.updateScheduleChangesSeen(changesSeen)
        }
    }

    private fun List<Session>.toChangeListParameter(): ChangeListParameter {
        val numDays = if (isEmpty()) 0 else repository.readMeta().numDays
        val useDeviceTimeZone = isNotEmpty() && repository.readUseDeviceTimeZoneEnabled()
        return ChangeListParameter(this, numDays, useDeviceTimeZone).also {
            logging.d(LOG_TAG, "Loaded $size changed sessions.")
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
