package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.favorites.FavoredSessionsViewEvent.OnBackClick
import nerd.tuxmobil.fahrplan.congress.search.SearchResultItem
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultProperty
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Success

/**
 * UI state class for FavoredSessionsScreen
 */
data class FavoredSessionsUiState(
    val searchResultState: SearchResultState,
    val isMultiSelectMode: Boolean,
    val checkedSessionIds: Map<String, Boolean>
)

@Composable
fun FavoredSessionsScreen(
    uiState: FavoredSessionsUiState,
    onViewEvent: (FavoredSessionsViewEvent) -> Unit
) {
    // Only handle back press when in multi-select mode
    BackHandler(enabled = uiState.isMultiSelectMode) {
        onViewEvent(OnBackClick)
    }
    
    EventFahrplanTheme {
        Scaffold(
            content = { contentPadding ->
                Box(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    when (val state = uiState.searchResultState) {
                        is Loading -> Loading()
                        is Success -> {
                            val sessions = state.parameters
                            if (sessions.isEmpty()) {
                                NoFavorites(onBack = { onViewEvent(OnBackClick) })
                            } else {
                                SearchResultList(
                                    parameters = sessions,
                                    isMultiSelectMode = uiState.isMultiSelectMode,
                                    checkedStates = uiState.checkedSessionIds,
                                    onItemClick = { sessionId -> 
                                        if (uiState.isMultiSelectMode) {
                                            onViewEvent(FavoredSessionsViewEvent.OnCheckedStateChange(sessionId))
                                        } else {
                                            onViewEvent(FavoredSessionsViewEvent.OnItemClick(sessionId))
                                        }
                                    },
                                    onItemLongClick = { sessionId ->
                                        onViewEvent(FavoredSessionsViewEvent.OnCheckedStateChange(sessionId))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun NoFavorites(onBack: () -> Unit) {
    NoData(
        emptyContent = R.drawable.no_favorites,
        title = stringResource(R.string.favorites_no_favorites_title),
        subtitle = stringResource(R.string.favorites_no_favorites_title),
    )
}

@Composable
fun SearchResultList(
    parameters: List<SearchResultParameter>,
    isMultiSelectMode: Boolean,
    checkedStates: Map<String, Boolean>,
    onItemClick: (String) -> Unit,
    onItemLongClick: (String) -> Unit
) {
    LazyColumn(state = rememberLazyListState()) {
        itemsIndexed(parameters) { index, parameter ->
            when (parameter) {
                is SearchResult -> {
                    val isChecked = checkedStates[parameter.id] ?: false
                    
                    CheckableItem(
                        checked = isChecked,
                        onCheckedChange = { onItemLongClick(parameter.id) },
                        onClick = { onItemClick(parameter.id) },
                        content = {
                            SearchResultItem(parameter)
                        }
                    )
                    val next = parameters.getOrNull(index + 1)
                    if (index < parameters.size - 1 && (next != null)) {
                        DividerHorizontal(Modifier.padding(horizontal = 12.dp))
                    }
                }
                else -> Unit // TODO
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckableItem(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = if (checked) colorResource(R.color.multi_choice_background) else Transparent
    Box(
        modifier = Modifier
            .background(color)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onCheckedChange() },
            ),
    ) {
        content()
    }
}

@MultiDevicePreview
@Composable
fun FavoredSessionsScreenPreview() {
    // Create sample data for preview
    val sampleState = FavoredSessionsUiState(
        searchResultState = Success(
            parameters = listOf(
                SearchResult(
                    id = "1",
                    title = SearchResultProperty("Lorem ipsum dolor sit amet", ""),
                    speakerNames = SearchResultProperty("Hedy Llamar", ""),
                    startsAt = SearchResultProperty("December 27, 2024 10:00", ""),
                ),
                SearchResult(
                    id = "3",
                    title = SearchResultProperty("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", ""),
                    speakerNames = SearchResultProperty("Jane Doe", ""),
                    startsAt = SearchResultProperty("December 27, 2024 18:30", "")
                ),
            )
        ),
        isMultiSelectMode = false,
        checkedSessionIds = emptyMap()
    )
    
    // Preview UI
    FavoredSessionsScreen(
        uiState = sampleState,
        onViewEvent = { }
    )
}

@MultiDevicePreview
@Composable
fun FavoredSessionsScreenEmptyPreview() {
    // Create empty data for preview
    val emptyState = FavoredSessionsUiState(
        searchResultState = Success(parameters = emptyList()),
        isMultiSelectMode = false,
        checkedSessionIds = emptyMap()
    )
    
    // Preview UI
    FavoredSessionsScreen(
        uiState = emptyState,
        onViewEvent = { }
    )
}

@MultiDevicePreview
@Composable
fun FavoredSessionsScreenLoadingPreview() {
    // Preview UI - loading state
    val loadingState = FavoredSessionsUiState(
        searchResultState = Loading,
        isMultiSelectMode = false,
        checkedSessionIds = emptyMap()
    )
    
    FavoredSessionsScreen(
        uiState = loadingState,
        onViewEvent = { }
    )
}
