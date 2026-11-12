package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesDestination.ConfirmDeleteAll
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesUiState.Loading
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesUiState.Success
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.Multiselect.OnDeleteSelectedClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.Multiselect.OnItemLongClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.Multiselect.OnSelectionModeDismiss
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.OnShareClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoritesViewEvent.OnShareToChaosflixClick
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameterFactory
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat

class FavoritesViewModel(
    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val searchResultParameterFactory: SearchResultParameterFactory,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<FavoritesUiState>(Loading)
    val uiState = mutableUiState.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val mutableEffects = Channel<FavoritesEffect>()
    val effects = mutableEffects.receiveAsFlow()

    init {
        launch {
            val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
            repository.favoriteSessions
                .map { searchResultParameterFactory.createSearchResults(it, useDeviceTimeZone) }
                .map { Success(it) }
                .collectLatest { mutableUiState.value = it }
        }
        launch {
            repository.favoriteSessions.collectLatest { sessions ->
                val validIds = sessions.map { it.sessionId }.toSet()
                _selectedIds.value = _selectedIds.value.intersect(validIds)
            }
        }
    }

    fun onViewEvent(viewEvent: FavoritesViewEvent) = when (viewEvent) {
        is OnItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
        is OnItemLongClick -> toggleSelection(viewEvent.sessionId)
        OnDeleteAllClick -> deleteAllFavorites()
        OnDeleteSelectedClick -> deleteSelectedFavorites()
        OnDeleteAllWithConfirmationClick -> navigateTo(ConfirmDeleteAll)
        OnSelectionModeDismiss -> clearSelection()
        OnShareClick -> share()
        OnShareToChaosflixClick -> shareToChaosflix()
    }

    private fun toggleSelection(sessionId: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            when (sessionId in this) {
                true -> remove(sessionId)
                false -> add(sessionId)
            }
        }
    }

    private fun deleteAllFavorites() {
        launch {
            repository.deleteAllFavorites()
        }
    }

    private fun deleteSelectedFavorites() {
        launch {
            repository.deleteFavorites(*_selectedIds.value.toTypedArray())
            clearSelection()
        }
    }

    private fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    private fun share() {
        launch {
            val timeZoneId = repository.readMeta().timeZoneId
            repository.favoriteSessions.collectLatest { sessions ->
                simpleSessionFormat.format(sessions, timeZoneId)?.let { formattedSessions ->
                    sendEffect(ShareSimple(formattedSessions))
                }
            }
        }
    }

    private fun shareToChaosflix() {
        launch {
            repository.favoriteSessions.collectLatest { sessions ->
                jsonSessionFormat.format(sessions)?.let { formattedSessions ->
                    sendEffect(ShareJson(formattedSessions))
                }
            }
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun navigateTo(destination: FavoritesDestination) {
        sendEffect(NavigateTo(destination))
    }

    private fun sendEffect(effect: FavoritesEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }

}
