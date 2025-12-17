package nerd.tuxmobil.fahrplan.congress.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.NoSearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchHistory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherTestExtension::class)
class SearchViewModelTest {

    @Nested
    inner class UiState {

        @Test
        fun `uiState emits loading state initially`() = runTest {
            // Use a sessionsFlow that never emits, so we can observe the loading state.
            val viewModel = createViewModel(sessionsFlow = MutableSharedFlow())

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState(resultsState = Loading))
            }
        }

        @Test
        fun `uiState emits search history when query is empty`() = runTest {
            val searchHistoryManager = createSearchHistoryManager()
            searchHistoryManager.append(scope = this, query = "foo")
            searchHistoryManager.append(scope = this, query = "bar")
            advanceUntilIdle()
            val viewModel = createViewModel(searchHistoryManager)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(
                    SearchUiState(
                        resultsState = SearchHistory(persistentListOf("bar", "foo")),
                    )
                )
            }
        }

        @Test
        fun `uiState emits 'no search results' when query and search history are empty`() =
            runTest {
                val viewModel = createViewModel()

                viewModel.uiState.test {
                    assertThat(awaitItem()).isEqualTo(
                        SearchUiState(
                            resultsState = NoSearchResults(backEvent = OnBackPress),
                        )
                    )
                }
            }

        @Test
        fun `uiState emits 'no search results' when no matching sessions were found`() = runTest {
            val viewModel = createViewModel()

            viewModel.onViewEvent(OnSearchQueryChange("foo"))

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(
                    SearchUiState(
                        query = "foo",
                        resultsState = NoSearchResults(backEvent = OnSearchSubScreenBackPress),
                    )
                )
            }
        }

        @Test
        fun `uiState emits list of search results when query matches sessions`() = runTest {
            val session1 = Session(
                sessionId = "1",
                title = "Title",
                speakers = listOf("Speakers"),
                startTime = Duration.ofMinutes(30),
            )
            val session2 = Session(
                sessionId = "2",
                title = "No Match",
                speakers = listOf("Jane", "Alice"),
                startTime = Duration.ofMinutes(60),
            )
            val sessions = listOf(session1, session2)
            val viewModel = createViewModel(sessionsFlow = flowOf(sessions))

            viewModel.onViewEvent(OnSearchQueryChange("title"))

            val expected = FakeSearchResultParameterFactory().createSearchResults(
                sessions = listOf(session1),
                useDeviceTimeZone = false,
            )
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(
                    SearchUiState(
                        query = "title",
                        resultsState = SearchResults(searchResults = expected.toImmutableList()),
                    )
                )
            }
        }

    }

    @Nested
    inner class Navigation {

        @Test
        fun `navigateBack emits when OnBackPress event is received`() = runTest {
            val viewModel = createViewModel()

            viewModel.onViewEvent(OnBackPress)

            viewModel.effects.test {
                assertThat(awaitItem()).isEqualTo(NavigateBack)
            }
        }

        @Test
        fun `navigates to session details when OnSearchResultItemClick event is received`() =
            runTest {
                val viewModel = createViewModel()

                viewModel.onViewEvent(OnSearchResultItemClick("42"))

                viewModel.effects.test {
                    assertThat(awaitItem()).isEqualTo(NavigateToSession("42"))
                }
            }

        @Test
        fun `query is empty string when OnBackIconClick event is received`() = runTest {
            val viewModel = createViewModel()
            viewModel.onViewEvent(OnSearchQueryChange("query"))

            viewModel.onViewEvent(OnBackIconClick)

            viewModel.uiState.test {
                assertThat(awaitItem().query).isEmpty()
            }
        }

        @Test
        fun `query is empty string when OnSearchSubScreenBackPress event is received`() = runTest {
            val viewModel = createViewModel()

            viewModel.onViewEvent(OnSearchSubScreenBackPress)

            viewModel.uiState.test {
                assertThat(awaitItem().query).isEmpty()
            }
        }

    }

    @Nested
    inner class History {

        @Test
        fun `search history is cleared when OnSearchHistoryClear event is received`() = runTest {
            val searchHistoryManager = createSearchHistoryManager()
            searchHistoryManager.append(scope = this, "irrelevant")
            advanceUntilIdle()
            val viewModel = createViewModel(searchHistoryManager)

            viewModel.onViewEvent(OnSearchHistoryClear)

            searchHistoryManager.searchHistory.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

    }

    @Nested
    inner class Query {

        @Test
        fun `query matches passed string when OnSearchHistoryItemClick event is received`() =
            runTest {
                val viewModel = createViewModel()

                viewModel.onViewEvent(OnSearchHistoryItemClick("query"))

                viewModel.uiState.test {
                    assertThat(awaitItem().query).isEqualTo("query")
                }
            }

        @Test
        fun `query matches passed string when OnSearchQueryChange event is received`() = runTest {
            val viewModel = createViewModel()

            viewModel.onViewEvent(OnSearchQueryChange("query"))

            viewModel.uiState.test {
                assertThat(awaitItem().query).isEqualTo("query")
            }
        }

        @Test
        fun `query is empty string when OnSearchQueryClear event is received`() = runTest {
            val viewModel = createViewModel()

            viewModel.onViewEvent(OnSearchQueryClear)

            viewModel.uiState.test {
                assertThat(awaitItem().query).isEmpty()
            }
        }

    }

    @Nested
    inner class Filters {
        @Test
        fun `all search filters are initially unselected`() = runTest {
            val favoriteFilter = IsFavoriteSearchFilter()
            val alarmFilter = HasAlarmSearchFilter()
            val viewModel = createViewModel(
                searchFilters = listOf(favoriteFilter, alarmFilter),
            )

            viewModel.uiState.test {
                assertThat(awaitItem().filters).containsExactly(
                    SearchFilterUiState(label = favoriteFilter.label, selected = false),
                    SearchFilterUiState(label = alarmFilter.label, selected = false),
                ).inOrder()
            }
        }

        @Test
        fun `clicking search filter toggles its selected state`() = runTest {
            val filter = IsFavoriteSearchFilter()
            val viewModel = createViewModel(
                searchFilters = listOf(filter),
            )

            viewModel.uiState.test {
                val initialState = awaitItem()

                // Select filter
                viewModel.onViewEvent(SearchViewEvent.OnFilterToggled(initialState.filters.first()))

                val filterSelectedState = awaitItem()
                assertThat(filterSelectedState.filters).containsExactly(
                    SearchFilterUiState(label = filter.label, selected = true)
                )

                // Deselect filter
                viewModel.onViewEvent(SearchViewEvent.OnFilterToggled(filterSelectedState.filters.first()))

                assertThat(awaitItem().filters).containsExactly(
                    SearchFilterUiState(label = filter.label, selected = false)
                )
            }
        }
    }

    private fun createSearchHistoryManager(): SearchHistoryManager {
        return SearchHistoryManager(InMemorySearchRepository())
    }

    private fun createViewModel(
        searchHistoryManager: SearchHistoryManager = createSearchHistoryManager(),
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
        searchFilters: List<SearchFilter> = emptyList(),
    ): SearchViewModel {
        return SearchViewModel(
            repository = createRepository(sessionsFlow),
            searchQueryFilter = SearchQueryFilter(),
            searchHistoryManager = searchHistoryManager,
            searchResultParameterFactory = FakeSearchResultParameterFactory(),
            searchFilters = searchFilters,
        )
    }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>>,
    ) = mock<AppRepository> {
        on { this.sessions } doReturn sessionsFlow
    }
}

private class FakeSearchResultParameterFactory : SearchResultParameterFactory {
    override fun createSearchResults(
        sessions: List<Session>,
        useDeviceTimeZone: Boolean
    ): List<SearchResultParameter> {
        return sessions.map { session ->
            val speakerNames = session.speakers.joinToString()
            val startTime = session.startTime.toWholeMinutes().toString()
            SearchResult(
                id = session.sessionId,
                title = SearchResultProperty(session.title, session.title),
                speakerNames = SearchResultProperty(speakerNames, speakerNames),
                startsAt = SearchResultProperty(startTime, startTime),
            )
        }
    }
}
