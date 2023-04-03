package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.livedata.SingleLiveEvent
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat

class StarredListViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat

) : ViewModel() {

    private companion object {
        const val LOG_TAG = "StarredListViewModel"
    }

    val starredListParameter: Flow<StarredListParameter> = repository.starredSessions
        .map { it.toStarredListParameter() }
        .flowOn(executionContext.database)

    val shareSimple = SingleLiveEvent<String>()
    val shareJson = SingleLiveEvent<String>()

    fun delete(session: Session) {
        launch {
            repository.updateHighlight(session)
        }
    }

    fun deleteAll() {
        launch {
            repository.deleteAllHighlights()
        }
    }

    fun share() {
        launch {
            val timeZoneId = repository.readMeta().timeZoneId
            repository.starredSessions.collect { sessions ->
                simpleSessionFormat.format(sessions, timeZoneId)?.let { formattedSessions ->
                    shareSimple.postValue(formattedSessions)
                }
            }
        }
    }

    fun shareToChaosflix() {
        launch {
            repository.starredSessions.collect { sessions ->
                jsonSessionFormat.format(sessions)?.let { formattedSessions ->
                    shareJson.postValue(formattedSessions)
                }
            }
        }
    }

    private fun List<Session>.toStarredListParameter(): StarredListParameter {
        val numDays = if (isEmpty()) 0 else repository.readMeta().numDays
        val useDeviceTimeZone = isNotEmpty() && repository.readUseDeviceTimeZoneEnabled()
        return StarredListParameter(this, numDays, useDeviceTimeZone).also {
            logging.d(LOG_TAG, "Loaded $size starred sessions.")
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
