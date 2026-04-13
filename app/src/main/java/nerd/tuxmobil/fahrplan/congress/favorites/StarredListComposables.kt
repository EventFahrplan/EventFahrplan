package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import nerd.tuxmobil.fahrplan.congress.commons.createSearchResultPreviewData
import nerd.tuxmobil.fahrplan.congress.commons.useVerticalFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderDayDate
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderSessionList
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.NavigationSection
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.NavigationSectionWithContent
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Loading
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Success
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnItemLongClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnSelectionModeDismiss
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchResultItem
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE
import nerd.tuxmobil.fahrplan.congress.utils.compose.ScrollPosition
import nerd.tuxmobil.fahrplan.congress.utils.compose.rememberAutoHideOnScrollDown

@Composable
fun StarredListContent(
    headerTitle: String,
    uiState: StarredListUiState,
    selectedIds: Set<String> = emptySet(),
    showInSidePane: Boolean,
    onBack: () -> Unit,
    onViewEvent: (StarredListViewEvent) -> Unit,
) {
    val useVerticalToolbar = useVerticalFloatingToolbar(showInSidePane)
    val navigationTitle = headerTitle.ifEmpty { stringResource(R.string.favorites_screen_default_title) }
    val multiselect = selectedIds.isNotEmpty()
    val hasStarredSessions = uiState is Success && uiState.parameters.isNotEmpty()
    val showFloatingToolbar = multiselect || hasStarredSessions

    Scaffold { _ ->
        Box(
            Modifier
                .fillMaxSize()
                .semantics { paneTitle = navigationTitle },
        ) {
            when (uiState) {
                is Loading -> {
                    Column(Modifier.fillMaxSize()) {
                        NavigationSection(
                            showInSidePane = showInSidePane,
                            onNavClick = onBack,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            Loading()
                        }
                    }
                }

                is Success -> {
                    val sessions = uiState.parameters
                    if (sessions.isEmpty()) {
                        Column(Modifier.fillMaxSize()) {
                            NavigationSection(
                                showInSidePane = showInSidePane,
                                onNavClick = onBack,
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                NoFavorites()
                            }
                        }
                        if (showFloatingToolbar) {
                            StarredListToolbar(
                                modifier = Modifier
                                    .align(if (useVerticalToolbar) CenterEnd else BottomCenter),
                                visible = true,
                                multiselect = multiselect,
                                hasStarredSessions = hasStarredSessions,
                                useVerticalToolbar = useVerticalToolbar,
                                onViewEvent = onViewEvent,
                            )
                        }
                    } else {
                        StarredSessionsList(
                            navigationTitle = navigationTitle,
                            sessions = sessions,
                            showInSidePane = showInSidePane,
                            useVerticalToolbar = useVerticalToolbar,
                            multiselect = multiselect,
                            selectedIds = selectedIds,
                            onBack = onBack,
                            onViewEvent = onViewEvent,
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun StarredSessionsList(
    navigationTitle: String,
    sessions: List<SearchResultParameter>,
    showInSidePane: Boolean,
    useVerticalToolbar: Boolean,
    multiselect: Boolean,
    selectedIds: Set<String>,
    onBack: () -> Unit,
    onViewEvent: (StarredListViewEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    var hasAutoScrolled by rememberSaveable { mutableStateOf(false) }
    val showToolbar by rememberAutoHideOnScrollDown(
        source = ScrollPosition.LazyList(listState),
        enabled = !useVerticalToolbar,
    )
    val hasStarredSessions = sessions.isNotEmpty()

    LaunchedEffect(sessions, hasAutoScrolled, listState) {
        if (sessions.isEmpty()) return@LaunchedEffect
        if (!hasAutoScrolled) {
            val firstFutureIndex = firstFutureItemIndex(sessions)
            if (firstFutureIndex in 1 until sessions.size) {
                // Lazy list item 0 is the top bar; session rows start at index 1.
                listState.scrollToItem(firstFutureIndex + 1)
            }
            hasAutoScrolled = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).only(Bottom).asPaddingValues(),
        ) {
            item {
                NavigationSectionWithContent(
                    showInSidePane = showInSidePane,
                    contentLeftOfCloseButton = {
                        HeaderSessionList(
                            modifier = Modifier.widthIn(max = maxWidth),
                            text = navigationTitle,
                            includeDefaultPadding = false,
                        )
                    },
                    contentRightOfBackButton = {
                        HeaderSessionList(
                            text = navigationTitle,
                            includeDefaultPadding = false,
                        )
                    },
                    onNavClick = {
                        if (multiselect) {
                            onViewEvent(OnSelectionModeDismiss)
                        } else {
                            onBack()
                        }
                    },
                )
            }
            searchResultItems(
                parameters = sessions,
                checkedStates = selectedIds.associateWith { true },
                useVerticalToolbar = useVerticalToolbar,
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
        StarredListToolbar(
            modifier = Modifier
                .align(if (useVerticalToolbar) CenterEnd else BottomCenter),
            visible = showToolbar,
            multiselect = multiselect,
            hasStarredSessions = hasStarredSessions,
            useVerticalToolbar = useVerticalToolbar,
            onViewEvent = onViewEvent,
        )
    }
}

@VisibleForTesting
internal fun firstFutureItemIndex(parameters: List<SearchResultParameter>) =
    parameters.indexOfFirst { it is SearchResult && it.startsAt.tenseType == FUTURE }

@Composable
private fun NoFavorites() {
    NoData(
        emptyContent = R.drawable.no_favorites,
        title = stringResource(R.string.favorites_no_favorites_title),
        subtitle = stringResource(R.string.favorites_no_favorites_subtitle),
    )
}

private fun LazyListScope.searchResultItems(
    parameters: List<SearchResultParameter>,
    checkedStates: Map<String, Boolean>,
    useVerticalToolbar: Boolean,
    onItemClick: (String) -> Unit,
    onItemLongClick: (String) -> Unit,
) {
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
                    content = {
                        SearchResultItem(
                            modifier = Modifier.padding(ToolbarMetrics.searchResultItemPaddingValues(useVerticalToolbar)),
                            searchResult = parameter,
                        )
                    },
                )
                val next = parameters.getOrNull(index + 1)
                if (index < parameters.size - 1 && (next is SearchResult)) {
                    DividerHorizontal()
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
            showInSidePane = false,
            onBack = { },
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
            onBack = { },
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
            onBack = { },
            onViewEvent = { },
        )
    }
}
