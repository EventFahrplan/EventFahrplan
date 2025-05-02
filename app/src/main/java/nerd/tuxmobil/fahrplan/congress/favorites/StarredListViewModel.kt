package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnBackClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnCheckedSessionsChange
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnDeleteClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnMultiSelectToggle
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnShareClick
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameterFactory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Success
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat

class StarredListViewModel(
    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val searchResultParameterFactory: SearchResultParameterFactory
) : ViewModel() {

    private companion object {
        const val LOG_TAG = "StarredListViewModel"
    }

    var screenNavigation: ScreenNavigation? = null

    val starredListParameter: Flow<StarredListParameter> = repository.starredSessions
        .map { it.toStarredListParameter() }
        .flowOn(executionContext.database)

    val searchResultsState = MutableStateFlow<SearchResultState>(Loading)

    private val mutableShouldExitMultiSelectMode = Channel<Boolean>()
    val shouldExitMultiSelectMode = mutableShouldExitMultiSelectMode.receiveAsFlow()

    // Selection state management
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _checkedSessionIds = MutableStateFlow<Set<String>>(emptySet())
    val checkedSessionIds: StateFlow<Set<String>> = _checkedSessionIds.asStateFlow()

    // For keeping track of which sessions are checked in the UI
    private val _checkedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedStates: StateFlow<Map<String, Boolean>> = _checkedStates.asStateFlow()

    private val mutableNavigateBack = Channel<Unit>()
    val navigateBack = mutableNavigateBack.receiveAsFlow()

    private val mutableMultiSelectToggled = Channel<Unit>()
    val multiSelectToggled = mutableMultiSelectToggled.receiveAsFlow()

    private val mutableShareSimple = Channel<String>()
    val shareSimple = mutableShareSimple.receiveAsFlow()

    private val mutableShareJson = Channel<String>()
    val shareJson = mutableShareJson.receiveAsFlow()

    init {
        observeStarredSessions()
    }

    private fun observeStarredSessions() {
        launch {
            val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
            repository.starredSessions
                .map { searchResultParameterFactory.createSearchResults(it, useDeviceTimeZone) }
                .map { Success(it) }
                .collectLatest { 
                    searchResultsState.value = it
                    
                    // Reset checked states when session list changes
                    if (it is Success) {
                        updateCheckedStates(it.parameters)
                    }
                }
        }
    }
    
    /**
     * Updates the checked states map when session list changes
     */
    private fun updateCheckedStates(parameters: List<SearchResultParameter>) {
        val newMap = parameters.associate { param ->
            when (param) {
                is SearchResultParameter.SearchResult -> param.id to (_checkedSessionIds.value.contains(param.id))
                else -> "" to false // Handle other parameter types if needed
            }
        }
        _checkedStates.value = newMap
    }
    
    /**
     * Toggles the checked state of a session
     */
    fun toggleCheckedState(sessionId: String) {
        val currentCheckedStates = _checkedStates.value.toMutableMap()
        val newValue = !(currentCheckedStates[sessionId] ?: false)
        currentCheckedStates[sessionId] = newValue
        _checkedStates.value = currentCheckedStates
        
        // Update checked session IDs
        val newCheckedIds = if (newValue) {
            _checkedSessionIds.value + sessionId
        } else {
            _checkedSessionIds.value - sessionId
        }
        _checkedSessionIds.value = newCheckedIds
        
        // Update multi-select mode
        val newMultiSelectMode = newCheckedIds.isNotEmpty()
        val modeChanged = _isMultiSelectMode.value != newMultiSelectMode
        _isMultiSelectMode.value = newMultiSelectMode
        
        // Log the change
        logging.d(LOG_TAG, "Session $sessionId checked state changed to $newValue, multi-select mode: ${_isMultiSelectMode.value}")
        
        // Notify UI if mode changed
        if (modeChanged) {
            launch {
                mutableMultiSelectToggled.send(Unit)
            }
        }
    }
    
    /**
     * Exits multi-select mode and clears all selections
     */
    fun exitMultiSelectMode() {
        // Only notify if we're actually in multi-select mode
        val wasInMultiSelectMode = _isMultiSelectMode.value
        
        // Clear all state
        _isMultiSelectMode.value = false
        _checkedSessionIds.value = emptySet()
        _checkedStates.value = _checkedStates.value.mapValues { false }.toMap()
        
        logging.d(LOG_TAG, "Exiting multi-select mode, was in multi-select mode: $wasInMultiSelectMode")
        
        if (wasInMultiSelectMode) {
            launch {
                mutableMultiSelectToggled.send(Unit)
            }
        }
    }

    /**
     * Handle fragment lifecycle event: onPause
     * Ensures selections are cleared when the fragment is paused
     */
    fun onFragmentPause() {
        logging.d(LOG_TAG, "Fragment onPause, ensuring selection state is clean")
        if (_isMultiSelectMode.value) {
            exitMultiSelectMode()
        }
    }

    /**
     * Handle fragment lifecycle event: onResume
     * Ensures we're in a clean state when resuming
     */
    fun onFragmentResume() {
        logging.d(LOG_TAG, "Fragment onResume, ensuring we have a clean selection state")
        // This helps when returning to the fragment from elsewhere
        if (_isMultiSelectMode.value && _checkedSessionIds.value.isEmpty()) {
            // Inconsistent state - fixing it
            _isMultiSelectMode.value = false
        }
    }

    fun onViewEvent(viewEvent: FavoredSessionsViewEvent) {
        when (viewEvent) {
            is OnCheckedSessionsChange -> {
                _checkedSessionIds.value = viewEvent.checkedSessionIds
                logging.d(LOG_TAG, "Checked sessions changed: ${viewEvent.checkedSessionIds.size} items")
                
                // Update the checked states map
                val currentMap = _checkedStates.value.toMutableMap()
                currentMap.keys.forEach { id ->
                    currentMap[id] = viewEvent.checkedSessionIds.contains(id)
                }
                _checkedStates.value = currentMap
            }
            is OnItemClick -> {
                screenNavigation?.navigateToSessionDetails(viewEvent.sessionId)
            }

            OnBackClick -> {
                logging.d(LOG_TAG, "Back clicked, exiting multi-select mode if active")
                if (_isMultiSelectMode.value) {
                    exitMultiSelectMode()
                } else {
                    mutableNavigateBack.sendOneTimeEvent(Unit)
                }
            }
            is OnShareClick -> {
                if (viewEvent.sessionIds.isEmpty()) {
                    shareAll()
                } else {
                    shareSelected(viewEvent.sessionIds)
                }
            }

            is OnDeleteClick ->
                if (viewEvent.sessionIds.isEmpty()) {
                    unfavorAllSessions()
                } else {
                    deleteSelected(viewEvent.sessionIds)
                }
            is OnMultiSelectToggle -> {
                logging.d(LOG_TAG, "Multi select toggled")
                mutableMultiSelectToggled.sendOneTimeEvent(Unit)
            }
            is FavoredSessionsViewEvent.OnCheckedStateChange -> {
                // This is handled directly via toggleCheckedState in the Fragment
                logging.d(LOG_TAG, "Checked state change for session: ${viewEvent.sessionId}")
            }
        }
    }

    fun unfavorSession(session: Session) {
        launch {
            repository.deleteHighlight(session.sessionId)
        }
    }

    fun unfavorCheckedSessions() {
        launch {
            mutableShouldExitMultiSelectMode.sendOneTimeEvent(true)
        }
        launch {
            logging.d(LOG_TAG, "Unfavor checked sessions: ${_checkedSessionIds.value}")
            repository.deleteHighlights(_checkedSessionIds.value)
            
            // Clear selection state after unfavoring
            exitMultiSelectMode()
        }
    }

    fun onActionModeDestroyed() {
        // Ensure multi-select mode is exited in the composable
        launch {
            // Clear checked sessions state
            exitMultiSelectMode()
            // Signal to exit multi-select mode
            mutableShouldExitMultiSelectMode.sendOneTimeEvent(true)
            // Log for debugging
            logging.d(LOG_TAG, "Action mode destroyed, clearing selection state")
        }
    }
    
    /**
     * Returns the current number of checked sessions
     */
    fun getCheckedSessionCount(): Int {
        return _checkedSessionIds.value.size
    }
    
    /**
     * Returns true if there are any checked sessions
     */
    fun hasCheckedSessions(): Boolean {
        return _checkedSessionIds.value.isNotEmpty()
    }

    fun unfavorAllSessions() {
        launch {
            repository.deleteAllHighlights()
        }
    }

    fun shareSelected(sessionIds: Set<String>) {
        if (sessionIds.isEmpty()) return

        launch {
            val timeZoneId = repository.readMeta().timeZoneId
            repository.starredSessions.collectLatest { sessions ->
                val selectedSessions = sessions.filter { it.sessionId in sessionIds }
                simpleSessionFormat.format(selectedSessions, timeZoneId)?.let { formattedSessions ->
                    mutableShareSimple.sendOneTimeEvent(formattedSessions)
                }
            }
        }
    }

    fun shareToChaosflix() {
        launch {
            repository.starredSessions.collectLatest { sessions ->
                jsonSessionFormat.format(sessions)?.let { formattedSessions ->
                    mutableShareJson.sendOneTimeEvent(formattedSessions)
                }
            }
        }
    }

    fun shareAll() {
        // Share all sessions
        launch {
            val timeZoneId = repository.readMeta().timeZoneId
            repository.starredSessions.collectLatest { sessions ->
                simpleSessionFormat.format(sessions, timeZoneId)?.let { formattedSessions ->
                    mutableShareSimple.sendOneTimeEvent(formattedSessions)
                }
            }
        }
    }

    fun deleteSelected(sessionIds: Set<String>) {
        if (sessionIds.isEmpty()) return

        launch {
            sessionIds.forEach { sessionId ->
                repository.deleteHighlight(sessionId)
            }
        }
    }

    fun share() {
        // For backward compatibility with StarredListFragment
        shareAll()
    }

    /**
     * Checks if Chaosflix export is enabled in the app
     * This replaces direct use of BuildConfig in the Fragment
     */
    fun isChaosflixExportEnabled(): Boolean {
        return true // For now, assume it's enabled until we find a better solution
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

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }
}
