package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.commons.createSearchResultPreviewData
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderDayDate
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderSessionList
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Loading
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Success
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnItemLongClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchResultItem
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE

@VisibleForTesting
internal fun firstFutureItemIndex(parameters: List<SearchResultParameter>) =
    parameters.indexOfFirst { it is SearchResult && it.startsAt.tenseType == FUTURE }

@Composable
fun StarredListContent(
    headerTitle: String,
    uiState: StarredListUiState,
    selectedIds: Set<String> = emptySet(),
    showInSidePane: Boolean,
    onViewEvent: (StarredListViewEvent) -> Unit,
) {
    Scaffold(
        content = {
            Box {
                when (uiState) {
                    is Loading -> Loading()
                    is Success -> {
                        val sessions = uiState.parameters
                        if (sessions.isEmpty()) {
                            NoFavorites()
                        } else {
                            SearchResultList(
                                headerTitle = headerTitle,
                                parameters = sessions,
                                checkedStates = selectedIds.associateWith { true },
                                showInSidePane = showInSidePane,
                                onItemClick = { sessionId ->
                                    when (selectedIds.isNotEmpty()) {
                                        true -> onViewEvent(OnItemLongClick(sessionId))
                                        false -> onViewEvent(OnItemClick(sessionId))
                                    }
                                },
                                onItemLongClick = { sessionId ->
                                    onViewEvent(OnItemLongClick(sessionId))
                                },
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun NoFavorites() {
    NoData(
        emptyContent = R.drawable.no_favorites,
        title = stringResource(R.string.favorites_no_favorites_title),
        subtitle = stringResource(R.string.favorites_no_favorites_subtitle),
    )
}

@Composable
private fun SearchResultList(
    headerTitle: String,
    parameters: List<SearchResultParameter>,
    checkedStates: Map<String, Boolean>,
    showInSidePane: Boolean,
    onItemClick: (String) -> Unit,
    onItemLongClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    var hasAutoScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(parameters, headerTitle, showInSidePane, hasAutoScrolled) {
        if (!hasAutoScrolled) {
            val firstFutureIndex = firstFutureItemIndex(parameters)
            if (firstFutureIndex in 1 until parameters.size) {
                val headerOffset = if (showInSidePane && headerTitle.isNotEmpty()) 1 else 0
                listState.scrollToItem(headerOffset + firstFutureIndex)
            }
            hasAutoScrolled = true
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).only(Bottom).asPaddingValues(),
    ) {
        if (showInSidePane && headerTitle.isNotEmpty()) {
            item {
                HeaderSessionList(headerTitle)
            }
        }
        itemsIndexed(parameters) { index, parameter ->
            when (parameter) {
                is Separator -> HeaderDayDate(
                    text = parameter.daySeparator.value,
                    contentDescription = parameter.daySeparator.contentDescription,
                )

                is SearchResult -> {
                    val isChecked = checkedStates[parameter.id] ?: false
                    CheckableItem(
                        checked = isChecked,
                        onCheckedChange = { onItemLongClick(parameter.id) },
                        onClick = { onItemClick(parameter.id) },
                        content = { SearchResultItem(parameter) }
                    )
                    val next = parameters.getOrNull(index + 1)
                    if (index < parameters.size - 1 && (next is SearchResult)) {
                        DividerHorizontal(Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CheckableItem(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = if (checked) EventFahrplanTheme.colorScheme.multiChoiceBackground else Transparent
    Box(
        modifier = modifier
            .background(color)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onCheckedChange() },
            )
            .safeContentHorizontalPadding(),
    ) {
        content()
    }
}

@MultiDevicePreview
@Composable
private fun StarredListContentPreview() {
    val uiState = Success(
        parameters = createSearchResultPreviewData(),
    )
    EventFahrplanTheme {
        StarredListContent(
            headerTitle = stringResource(R.string.favorites_screen_default_title),
            uiState = uiState,
            showInSidePane = true,
            onViewEvent = { },
        )
    }
}

@Preview
@Composable
private fun StarredListContentEmptyPreview() {
    EventFahrplanTheme {
        StarredListContent(
            headerTitle = "",
            uiState = Success(emptyList()),
            showInSidePane = false,
            onViewEvent = { },
        )
    }
}

@Preview
@Composable
private fun StarredListContentLoadingPreview() {
    EventFahrplanTheme {
        StarredListContent(
            headerTitle = "",
            uiState = Loading,
            showInSidePane = false,
            onViewEvent = { },
        )
    }
}
