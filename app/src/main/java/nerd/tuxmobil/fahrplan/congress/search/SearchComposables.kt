package nerd.tuxmobil.fahrplan.congress.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.Loading
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonOutlined
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.search.InputField
import nerd.tuxmobil.fahrplan.congress.designsystem.search.SearchBar
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.ListItem
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextHeadlineContent
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextOverline
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextSupportingContent
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Success
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress

@Composable
fun SearchScreen(
    searchQuery: String,
    searchHistory: List<String>,
    state: SearchResultState,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    EventFahrplanTheme {
        Scaffold { contentPadding ->
            Box(
                Modifier
                    .padding(contentPadding)
            ) {
                val expanded = true
                SearchBar(
                    modifier = Modifier
                        .align(TopCenter)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchQueryInputField(
                            expanded = expanded,
                            searchQuery = searchQuery,
                            onViewEvent = onViewEvent,
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = { },
                    content = {
                        SearchBarContent(
                            state = state,
                            searchQuery = searchQuery,
                            searchHistory = searchHistory,
                            onViewEvent = onViewEvent,
                        )
                    },
                )
                BackHandler {
                    onViewEvent(OnBackPress)
                }
            }
        }
    }
}

@Composable
private fun SearchBarContent(
    state: SearchResultState,
    searchQuery: String,
    searchHistory: List<String>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    when (state) {
        is Loading -> Loading()
        is Success -> {
            val sessions = state.parameters
            if (sessions.isEmpty()) {
                val isSearchQueryEmpty = searchQuery.isEmpty()
                if (isSearchQueryEmpty && searchHistory.isNotEmpty()) {
                    SearchHistoryList(searchHistory, onViewEvent)
                } else {
                    NoSearchResult {
                        val event = if (isSearchQueryEmpty) OnBackPress else OnSearchSubScreenBackPress
                        onViewEvent(event)
                    }
                }
            } else {
                SearchResultList(sessions, onViewEvent)
            }
        }
    }
}

@Composable
private fun SearchQueryInputField(
    @Suppress("SameParameterValue") expanded: Boolean,
    searchQuery: String,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    InputField(
        modifier = Modifier.focusRequester(focusRequester),
        query = searchQuery,
        onQueryChange = { onViewEvent(OnSearchQueryChange(it)) },
        onSearch = { keyboardController?.hide() },
        expanded = expanded,
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
    parameters: List<SearchResultParameter>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    LazyColumn(state = rememberLazyListState()) {
        itemsIndexed(parameters) { index, parameter ->
            when (parameter) {
                is SearchResult -> {
                    SearchResultItem(parameter, Modifier.clickable {
                        onViewEvent(OnSearchResultItemClick(parameter.id))
                    })
                    val next = parameters.getOrNull(index + 1)
                    if (index < parameters.size - 1 && (next != null)) {
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
            )
        },
        headlineContent = {
            TextHeadlineContent(
                modifier = Modifier.semantics {
                    contentDescription = searchResult.title.contentDescription
                },
                text = searchResult.title.value,
            )
        },
        supportingContent = {
            if (searchResult.speakerNames.value.isNotEmpty()) {
                TextSupportingContent(
                    modifier = Modifier.semantics {
                        contentDescription = searchResult.speakerNames.contentDescription
                    },
                    text = searchResult.speakerNames.value,
                )
            }
        },
    )
}

@Composable
private fun SearchHistoryList(
    searchQueries: List<String>,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.search_history_title),
            fontWeight = Bold,
            modifier = Modifier.weight(1f),
        )
        ButtonOutlined(onClick = { onViewEvent(OnSearchHistoryClear) }) {
            Text(
                stringResource(R.string.search_history_clear),
            )
        }
    }
    LazyColumn(state = rememberLazyListState()) {
        itemsIndexed(searchQueries) { index, searchQuery ->
            SearchHistoryItem(searchQuery, Modifier.clickable {
                onViewEvent(OnSearchHistoryItemClick(searchQuery))
            })
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
private fun SearchScreenPreview() {
    SearchScreen(
        searchQuery = "Lorem ipsum",
        searchHistory = emptyList(),
        state = Success(
            listOf(
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
        onViewEvent = { },
    )
}

@Preview
@Composable
private fun SearchScreenHistoryPreview() {
    SearchScreen(
        searchQuery = "",
        searchHistory = listOf("Lorem ipsum", "Dolor sit amet"),
        state = Success(emptyList()),
        onViewEvent = { },
    )
}

@Preview
@Composable
private fun SearchScreenEmptyPreview() {
    SearchScreen(
        searchQuery = "foobar",
        searchHistory = emptyList(),
        state = Success(emptyList()),
        onViewEvent = { },
    )
}

@Preview
@Composable
private fun SearchScreenLoadingPreview() {
    SearchScreen(
        searchQuery = "",
        searchHistory = emptyList(),
        state = Loading,
        onViewEvent = { },
    )
}
