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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.commons.Loading
import nerd.tuxmobil.fahrplan.congress.commons.NoData
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    darkMode: Boolean,
    searchQuery: String,
    searchHistory: List<String>,
    state: SearchResultState,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    EventFahrplanTheme(darkMode = darkMode) {
        Scaffold { contentPadding ->
            Box(
                Modifier
                    .padding(contentPadding)
            ) {
                val expanded = true
                val containerBackgroundColor = colorResource(if (darkMode) R.color.search_background_dark else R.color.search_background_light)
                val dividerColor = colorResource(if (darkMode) R.color.search_divider_dark else R.color.search_divider_light)
                SearchBar(
                    modifier = Modifier
                        .align(TopCenter)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchQueryInputField(
                            darkMode = darkMode,
                            expanded = expanded,
                            searchQuery = searchQuery,
                            onViewEvent = onViewEvent,
                        )
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = containerBackgroundColor,
                        dividerColor = dividerColor,
                    ),
                    expanded = expanded,
                    onExpandedChange = { },
                    content = {
                        SearchBarContent(
                            state = state,
                            searchQuery = searchQuery,
                            searchHistory = searchHistory,
                            darkMode = darkMode,
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
    @Suppress("SameParameterValue") darkMode: Boolean,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    when (state) {
        is Loading -> Loading()
        is Success -> {
            val sessions = state.parameters
            if (sessions.isEmpty()) {
                val isSearchQueryEmpty = searchQuery.isEmpty()
                if (isSearchQueryEmpty && searchHistory.isNotEmpty()) {
                    SearchHistoryList(darkMode, searchHistory, onViewEvent)
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
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchQueryInputField(
    @Suppress("SameParameterValue") darkMode: Boolean,
    @Suppress("SameParameterValue") expanded: Boolean,
    searchQuery: String,
    onViewEvent: (SearchViewEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val iconTintColor = colorResource(if (darkMode) R.color.search_icon_dark else R.color.search_icon_light)
    val textColor = colorResource(if (darkMode) R.color.search_query_text_dark else R.color.search_query_text_light)

    SearchBarDefaults.InputField(
        modifier = Modifier.focusRequester(focusRequester),
        query = searchQuery,
        onQueryChange = { onViewEvent(OnSearchQueryChange(it)) },
        onSearch = { keyboardController?.hide() },
        expanded = expanded,
        onExpandedChange = { },
        placeholder = { Text(stringResource(R.string.search_query_hint)) },
        leadingIcon = {
            if (searchQuery.isEmpty()) {
                SearchIcon(iconTintColor)
            } else {
                IconButton(onClick = { onViewEvent(OnBackIconClick) }) {
                    BackIcon(iconTintColor)
                }
            }
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onViewEvent(OnSearchQueryClear) }) {
                    ClearSearchQueryIcon(iconTintColor)
                }
            }
        },
        colors = inputFieldColors(
            cursorColor = textColor,
        )
    )

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchIcon(iconTintColor: Color) {
    Icon(
        Icons.Default.Search,
        contentDescription = null,
        tint = iconTintColor,
    )
}

@Composable
private fun BackIcon(iconTintColor: Color) {
    Icon(
        Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = stringResource(R.string.navigate_back_content_description),
        tint = iconTintColor,
    )
}

@Composable
private fun ClearSearchQueryIcon(iconTintColor: Color) {
    Icon(
        Icons.Default.Close,
        contentDescription = stringResource(R.string.search_clear_search_query_content_description),
        tint = iconTintColor,
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
                        HorizontalDivider(Modifier.padding(horizontal = 12.dp))
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
            Text(
                modifier = Modifier.semantics {
                    contentDescription = searchResult.startsAt.contentDescription
                },
                text = searchResult.startsAt.value,
            )
        },
        headlineContent = {
            Text(
                modifier = Modifier.semantics {
                    contentDescription = searchResult.title.contentDescription
                },
                text = searchResult.title.value,
                overflow = Ellipsis,
                maxLines = 1,
            )
        },
        supportingContent = {
            if (searchResult.speakerNames.value.isNotEmpty()) {
                Text(
                    modifier = Modifier.semantics {
                        contentDescription = searchResult.speakerNames.contentDescription
                    },
                    text = searchResult.speakerNames.value,
                    overflow = Ellipsis,
                    maxLines = 1,
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = colorResource(android.R.color.transparent),
        ),
    )
}

@Composable
private fun SearchHistoryList(
    @Suppress("SameParameterValue") darkMode: Boolean,
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
        OutlinedButton(onClick = { onViewEvent(OnSearchHistoryClear) }) {
            Text(
                stringResource(R.string.search_history_clear),
                color = colorResource(if (darkMode) R.color.search_history_clear_dark else R.color.search_history_clear_light),
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
                HorizontalDivider(Modifier.padding(horizontal = 12.dp))
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
            Text(
                text = searchQuery,
                overflow = Ellipsis,
                maxLines = 1,
            )
        },
        trailingContent = { InsertSearchHistoryIcon() },
        colors = ListItemDefaults.colors(
            containerColor = colorResource(android.R.color.transparent),
        ),
    )
}

@Composable
private fun InsertSearchHistoryIcon() {
    Icon(
        painterResource(R.drawable.ic_arrow_north_west),
        contentDescription = null,
    )
}

@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreen(
        darkMode = false,
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
        darkMode = false,
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
        darkMode = false,
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
        darkMode = false,
        searchQuery = "",
        searchHistory = emptyList(),
        state = Loading,
        onViewEvent = { },
    )
}
