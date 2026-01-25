package nerd.tuxmobil.fahrplan.congress.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import info.metadude.android.eventfahrplan.commons.flow.observe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonOutlined
import nerd.tuxmobil.fahrplan.congress.designsystem.chips.FilterChip
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderDayDate
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.search.InputField
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.ListItem
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextHeadlineContent
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextOverline
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextSupportingContent
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.NoSearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchHistory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnFilterToggled
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE
import nerd.tuxmobil.fahrplan.congress.search.TenseType.PAST

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(context = LocalContext.current),
    ),
    onBack: () -> Unit,
    onSessionListClick: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                NavigateBack -> onBack()
                is NavigateToSession -> onSessionListClick(effect.sessionId)
            }
        }
    }

    val state by viewModel.uiState.collectAsState()

    SearchContent(
        state = state,
        onViewEvent = viewModel::onViewEvent,
    )
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            SearchBox(
                state = state,
                onViewEvent = onViewEvent,
            )

            DividerHorizontal(color = EventFahrplanTheme.colorScheme.searchBarDivider)

            SearchBarContent(
                state = state.resultsState,
                onViewEvent = onViewEvent,
            )
        }

        BackHandler {
            onViewEvent(OnBackPress)
        }
    }
}

@Composable
fun SearchBox(
    state: SearchUiState,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SearchQueryInputField(
            searchQuery = state.query,
            onViewEvent = onViewEvent,
        )

        SearchFilters(state.filters, onViewEvent)
    }
}

@Composable
private fun SearchFilters(
    filters: ImmutableList<SearchFilterUiState>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .safeContentHorizontalPadding(),
    ) {
        for (searchFilter in filters) {
            FilterChip(
                onClick = { onViewEvent(OnFilterToggled(searchFilter)) },
                label = { Text(stringResource(searchFilter.label)) },
                selected = searchFilter.selected,
                selectedIcon = Icons.Filled.Done,
            )
        }
    }
}

@Composable
private fun SearchBarContent(
    state: SearchResultState,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    when (state) {
        is Loading -> Loading()
        is NoSearchResults -> NoSearchResult(onBack = { onViewEvent(state.backEvent) })
        is SearchHistory -> SearchHistoryList(state.searchTerms, onViewEvent)
        is SearchResults -> SearchResultList(state.searchResults, onViewEvent)
    }
}

@Composable
private fun SearchQueryInputField(
    searchQuery: String,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    InputField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .safeContentHorizontalPadding(),
        query = searchQuery,
        onQueryChange = { onViewEvent(OnSearchQueryChange(it)) },
        onSearch = { keyboardController?.hide() },
        expanded = true,
        onExpandedChange = { },
        placeholder = { Text(stringResource(R.string.search_query_hint)) },
        leadingIcon = {
            if (searchQuery.isEmpty()) {
                SearchIcon()
            } else {
                val backContentDescription = stringResource(R.string.navigate_back_content_description)
                ButtonIcon(
                    modifier = Modifier.semantics {
                        contentDescription = backContentDescription
                    },
                    onClick = { onViewEvent(OnBackIconClick) }
                ) {
                    BackIcon()
                }
            }
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                val clearContentDescription = stringResource(R.string.search_clear_search_query_content_description)
                ButtonIcon(
                    modifier = Modifier.semantics {
                        contentDescription = clearContentDescription
                    },
                    onClick = { onViewEvent(OnSearchQueryClear) }
                ) {
                    ClearSearchQueryIcon()
                }
            }
        },
    )

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchIcon() {
    IconDecorativeVector(
        Icons.Default.Search,
    )
}

@Composable
private fun BackIcon() {
    IconDecorativeVector(
        Icons.AutoMirrored.Default.ArrowBack,
    )
}

@Composable
private fun ClearSearchQueryIcon() {
    IconDecorativeVector(
        Icons.Default.Close,
    )
}

@Composable
private fun NoSearchResult(onBack: () -> Unit) {
    NoData(
        emptyContent = R.drawable.no_search_results,
        title = stringResource(R.string.search_no_search_result_title),
        subtitle = stringResource(R.string.search_no_search_result_subtitle),
    )
    BackHandler {
        onBack()
    }
}

@Composable
private fun SearchResultList(
    parameters: ImmutableList<SearchResultParameter>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).only(Bottom).asPaddingValues(),
    ) {
        itemsIndexed(parameters) { index, parameter ->
            when (parameter) {
                is Separator -> HeaderDayDate(
                    text = parameter.daySeparator.value,
                    contentDescription = parameter.daySeparator.contentDescription,
                )

                is SearchResult -> {
                    SearchResultItem(
                        searchResult = parameter,
                        modifier = Modifier
                            .clickable { onViewEvent(OnSearchResultItemClick(parameter.id)) }
                            .safeContentHorizontalPadding()
                    )
                    val next = parameters.getOrNull(index + 1)
                    if (index < parameters.size - 1 && next is SearchResult) {
                        DividerHorizontal(Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
    BackHandler {
        onViewEvent(OnSearchSubScreenBackPress)
    }
}

@Composable
private fun SearchResultItem(
    searchResult: SearchResult,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        overlineContent = {
            TextOverline(
                modifier = Modifier.semantics {
                    contentDescription = searchResult.startsAt.contentDescription
                },
                text = searchResult.startsAt.value,
                color = searchResult.startsAt.tenseType.color(),
            )
        },
        headlineContent = {
            TextHeadlineContent(
                modifier = Modifier.semantics {
                    contentDescription = searchResult.title.contentDescription
                },
                text = searchResult.title.value,
                color = searchResult.title.tenseType.color(),
            )
        },
        supportingContent = {
            if (searchResult.speakerNames.value.isNotEmpty()) {
                TextSupportingContent(
                    modifier = Modifier.semantics {
                        contentDescription = searchResult.speakerNames.contentDescription
                    },
                    text = searchResult.speakerNames.value,
                    color = searchResult.speakerNames.tenseType.color(),
                )
            }
        },
    )
}

@Composable
private fun TenseType.color() = when (this) {
    PAST -> EventFahrplanTheme.colorScheme.textPastContent
    FUTURE -> Color.Unspecified
}

@Composable
private fun SearchHistoryList(
    searchQueries: ImmutableList<String>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .safeContentHorizontalPadding(),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.search_history_title),
            fontWeight = Bold,
            modifier = Modifier.weight(1f),
        )
        ButtonOutlined(
            border = BorderStroke(1.dp, EventFahrplanTheme.colorScheme.primary),
            onClick = { onViewEvent(OnSearchHistoryClear) }
        ) {
            Text(
                stringResource(R.string.search_history_clear),
            )
        }
    }
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).only(Bottom).asPaddingValues(),
    ) {
        itemsIndexed(searchQueries) { index, searchQuery ->
            SearchHistoryItem(
                searchQuery = searchQuery,
                modifier = Modifier
                    .clickable { onViewEvent(OnSearchHistoryItemClick(searchQuery)) }
                    .safeContentHorizontalPadding()
            )
            val next = searchQueries.getOrNull(index + 1)
            if (index < searchQueries.size - 1 && (next != null)) {
                DividerHorizontal(Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
private fun SearchHistoryItem(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            TextHeadlineContent(
                text = searchQuery,
            )
        },
        trailingContent = { InsertSearchHistoryIcon() },
    )
}

@Composable
private fun InsertSearchHistoryIcon() {
    IconDecorative(
        icon = R.drawable.ic_arrow_north_west,
    )
}

@MultiDevicePreview
@Composable
private fun SearchContentPreview() {
    EventFahrplanTheme {
        SearchContent(
            state = SearchUiState(
                query = "Lorem ipsum",
                filters = searchFilters(),
                resultsState = SearchResults(
                    persistentListOf(
                        Separator(
                            DaySeparatorProperty(
                                value = "DAY 1 - 12/27/2024",
                                contentDescription = "Day 1 - December 27, 2024",
                            )
                        ),
                        SearchResult(
                            id = "1",
                            title = SearchResultProperty("Lorem ipsum dolor sit amet", ""),
                            speakerNames = SearchResultProperty("Hedy Llamar", ""),
                            startsAt = SearchResultProperty("December 27, 2024 10:00", ""),
                        ),
                        SearchResult(
                            id = "2",
                            title = SearchResultProperty("Dolor sit amet", ""),
                            speakerNames = SearchResultProperty("Hedy Llamar", ""),
                            startsAt = SearchResultProperty("December 27, 2024 12:00", ""),
                        ),
                        Separator(
                            DaySeparatorProperty(
                                value = "DAY 2 - 12/28/2024",
                                contentDescription = "Day 2 - December 28, 2024",
                            )
                        ),
                        SearchResult(
                            id = "3",
                            title = SearchResultProperty(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                ""
                            ),
                            speakerNames = SearchResultProperty("Jane Doe", ""),
                            startsAt = SearchResultProperty("December 28, 2024 18:30", "")
                        ),
                    )
                )
            ),
            onViewEvent = { },
        )
    }
}

@Preview
@Composable
private fun SearchContentHistoryPreview() {
    EventFahrplanTheme {
        SearchContent(
            state = SearchUiState(
                query = "",
                filters = searchFilters(),
                resultsState = SearchHistory(
                    searchTerms = persistentListOf(
                        "Lorem ipsum",
                        "Dolor sit amet",
                    ),
                )
            ),
            onViewEvent = { },
        )
    }
}

@Preview
@Composable
private fun SearchContentEmptyPreview() {
    EventFahrplanTheme {
        SearchContent(
            state = SearchUiState(
                query = "foobar",
                filters = searchFilters(),
                resultsState = NoSearchResults(OnSearchSubScreenBackPress)
            ),
            onViewEvent = { },
        )
    }
}

@Preview
@Composable
private fun SearchContentLoadingPreview() {
    EventFahrplanTheme {
        SearchContent(
            state = SearchUiState(
                query = "",
                filters = searchFilters(),
                resultsState = Loading,
            ),
            onViewEvent = { },
        )
    }
}

private fun searchFilters(): ImmutableList<SearchFilterUiState> {
    return persistentListOf(
        SearchFilterUiState(label = R.string.search_filter_is_favorite, selected = false),
        SearchFilterUiState(label = R.string.search_filter_has_alarm, selected = false),
        SearchFilterUiState(label = R.string.search_filter_not_recorded, selected = false),
        SearchFilterUiState(label = R.string.search_filter_within_speaker_names, selected = false),
    )
}
