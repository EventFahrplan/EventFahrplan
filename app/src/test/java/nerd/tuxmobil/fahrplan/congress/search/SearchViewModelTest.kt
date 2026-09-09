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
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
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
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnFilterToggled
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnLanguageFilterToggled
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import nerd.tuxmobil.fahrplan.congress.search.languages.FakeLanguageResourceResolver
import nerd.tuxmobil.fahrplan.congress.search.languages.LanguageOption
import nerd.tuxmobil.fahrplan.congress.search.languages.SYNTHETIC_LANGUAGE_KEY_OTHER
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFilterUiState
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFiltersState
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
                        languageFilter = SearchLanguageFilterUiState(
                            languageOptions = persistentListOf(
                                LanguageOption(
                                    filterKey = SYNTHETIC_LANGUAGE_KEY_OTHER,
                                    displayName = "Other",
                                    selected = false,
                                ),
                            ),
                        ),
                        resultsState = SearchResults(searchResults = expected.toImmutableList()),
                    )
                )
            }
        }

    }

    @Nested
    inner class LanguageFilter {

        @Test
        fun `uiState exposes language options derived from sessions`() = runTest {
            val sessions = listOf(
                Session(sessionId = "1", language = "en"),
                Session(sessionId = "2", language = "de, en"),
                Session(sessionId = "3", language = ""),
            )
            val viewModel = createViewModel(sessionsFlow = flowOf(sessions))

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.languageFilter?.languageOptions?.map { it.filterKey })
                    .containsExactly("de, en", "en", SYNTHETIC_LANGUAGE_KEY_OTHER)
                    .inOrder()
            }
        }

        @Test
        fun `uiState filters sessions by selected language without query`() = runTest {
            val englishSession = Session(sessionId = "1", title = "English talk", language = "en")
            val germanSession = Session(sessionId = "2", title = "German talk", language = "de")
            val sessions = listOf(englishSession, germanSession)
            val viewModel = createViewModel(sessionsFlow = flowOf(sessions))

            viewModel.onViewEvent(OnLanguageFilterToggled("en"))

            val expected = FakeSearchResultParameterFactory().createSearchResults(
                sessions = listOf(englishSession),
                useDeviceTimeZone = false,
            )
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(
                    SearchUiState(
                        languageFilter = SearchLanguageFilterUiState(
                            languageOptions = persistentListOf(
                                LanguageOption(filterKey = "de", displayName = "German", selected = false),
                                LanguageOption(filterKey = "en", displayName = "English", selected = true),
                            ),
                        ),
                        resultsState = SearchResults(searchResults = expected.toImmutableList()),
                    )
                )
            }
        }

        @Test
        fun `back press unselects last selected language before navigating back`() = runTest {
            val sessions = listOf(
                Session(sessionId = "1", language = "en"),
                Session(sessionId = "2", language = "de"),
            )
            val viewModel = createViewModel(sessionsFlow = flowOf(sessions))

            viewModel.onViewEvent(OnLanguageFilterToggled("en"))
            viewModel.onViewEvent(OnLanguageFilterToggled("de"))
            viewModel.onViewEvent(OnBackPress)

            viewModel.uiState.test {
                val languageFilter = awaitItem().languageFilter
                assertThat(languageFilter?.selectedCount).isEqualTo(1)
                assertThat(languageFilter?.languageOptions?.single { it.selected }?.filterKey).isEqualTo("en")
            }
            viewModel.effects.test {
                expectNoEvents()
            }
        }

        @Test
        fun `back press clears most recently selected regular filter after language`() = runTest {
            val sessions = listOf(Session(sessionId = "1", language = "en"))
            val viewModel = createViewModel(
                sessionsFlow = flowOf(sessions),
                searchFilters = SUPPORTED_SEARCH_FILTERS,
            )

            viewModel.onViewEvent(OnLanguageFilterToggled("en"))
            viewModel.onViewEvent(
                OnFilterToggled(SearchFilterUiState(R.string.search_filter_recorded, selected = false))
            )
            viewModel.onViewEvent(OnBackPress)

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filters.single { it.label == R.string.search_filter_recorded }.selected).isFalse()
                assertThat(state.languageFilter?.selectedCount).isEqualTo(1)
            }
        }

        @Test
        fun `back press clears most recently selected language after regular filter`() = runTest {
            val sessions = listOf(Session(sessionId = "1", language = "en"))
            val viewModel = createViewModel(
                sessionsFlow = flowOf(sessions),
                searchFilters = SUPPORTED_SEARCH_FILTERS,
            )

            viewModel.onViewEvent(
                OnFilterToggled(SearchFilterUiState(R.string.search_filter_recorded, selected = false))
            )
            viewModel.onViewEvent(OnLanguageFilterToggled("en"))
            viewModel.onViewEvent(OnBackPress)

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.filters.single { it.label == R.string.search_filter_recorded }.selected).isTrue()
                assertThat(state.languageFilter?.selectedCount).isEqualTo(0)
            }
        }

        @Test
        fun `uiState filters sessions by multiple selected languages (OR combination)`() = runTest {
            val englishSession = Session(sessionId = "1", title = "English talk", language = "en")
            val germanSession = Session(sessionId = "2", title = "German talk", language = "de")
            val frenchSession = Session(sessionId = "3", title = "French talk", language = "fr")
            val viewModel = createViewModel(
                sessionsFlow = flowOf(listOf(englishSession, germanSession, frenchSession)),
            )

            viewModel.onViewEvent(OnLanguageFilterToggled("en"))
            viewModel.onViewEvent(OnLanguageFilterToggled("de"))

            val expected = FakeSearchResultParameterFactory().createSearchResults(
                sessions = listOf(englishSession, germanSession),
                useDeviceTimeZone = false,
            )
            viewModel.uiState.test {
                assertThat(awaitItem().resultsState).isEqualTo(
                    SearchResults(searchResults = expected.toImmutableList())
                )
            }
        }

        @Test
        fun `uiState filters sessions with empty language when Other is selected`() = runTest {
            val otherSession = Session(sessionId = "1", title = "Unknown language", language = "")
            val englishSession = Session(sessionId = "2", title = "English talk", language = "en")
            val viewModel = createViewModel(
                sessionsFlow = flowOf(listOf(otherSession, englishSession)),
            )

            viewModel.onViewEvent(OnLanguageFilterToggled(SYNTHETIC_LANGUAGE_KEY_OTHER))

            val expected = FakeSearchResultParameterFactory().createSearchResults(
                sessions = listOf(otherSession),
                useDeviceTimeZone = false,
            )
            viewModel.uiState.test {
                assertThat(awaitItem().resultsState).isEqualTo(
                    SearchResults(searchResults = expected.toImmutableList())
                )
            }
        }

        @Test
        fun `uiState filters sessions which satisfy both language filter and other filters (AND combination)`() =
            runTest {
                val favoriteEnglish = Session(sessionId = "1", title = "Favorite English", language = "en", isHighlight = true)
                val favoriteGerman = Session(sessionId = "2", title = "Favorite German", language = "de", isHighlight = true)
                val notFavoriteEnglish = Session(sessionId = "3", title = "Other English", language = "en")
                val viewModel = createViewModel(
                    sessionsFlow = flowOf(listOf(favoriteEnglish, favoriteGerman, notFavoriteEnglish)),
                    searchFilters = SUPPORTED_SEARCH_FILTERS,
                )

                viewModel.onViewEvent(
                    OnFilterToggled(SearchFilterUiState(label = R.string.search_filter_is_favorite, selected = false))
                )
                viewModel.onViewEvent(OnLanguageFilterToggled("en"))

                val expected = FakeSearchResultParameterFactory().createSearchResults(
                    sessions = listOf(favoriteEnglish),
                    useDeviceTimeZone = false,
                )
                viewModel.uiState.test {
                    assertThat(awaitItem().resultsState).isEqualTo(
                        SearchResults(searchResults = expected.toImmutableList())
                    )
                }
            }

        @Test
        fun `selected language stays unselected after disappearing and returning in session refreshes`() = runTest {
            // Provide refreshable sessions with English and German initially available.
            val sessionsFlow = MutableSharedFlow<List<Session>>(replay = 1)
            sessionsFlow.emit(
                listOf(
                    Session(sessionId = "1", language = "en"),
                    Session(sessionId = "2", language = "de"),
                )
            )
            // Keep access to the stored selection so the test can verify it is cleared.
            val languageFiltersState = SearchLanguageFiltersState()
            val viewModel = createViewModel(
                sessionsFlow = sessionsFlow,
                languageFiltersState = languageFiltersState,
            )

            // Select English before refreshing the available languages.
            viewModel.onViewEvent(OnLanguageFilterToggled("en"))

            viewModel.uiState.test {
                // Confirm the English filter is active in the initial UI state.
                assertThat(awaitItem().languageFilter?.selectedCount).isEqualTo(1)

                // Remove English and wait for the UI to reflect the cleared selection.
                sessionsFlow.emit(listOf(Session(sessionId = "2", language = "de")))
                var prunedLanguageFilter = awaitItem().languageFilter
                while (prunedLanguageFilter?.selectedCount != 0) {
                    prunedLanguageFilter = awaitItem().languageFilter
                }
                // Verify the unavailable language is also removed from the stored selection.
                assertThat(languageFiltersState.selectedLanguageKeys.value).isEmpty()

                // Restore English and wait for it to reappear as an available option.
                sessionsFlow.emit(listOf(Session(sessionId = "1", language = "en")))
                var restoredLanguageFilter = awaitItem().languageFilter
                while (restoredLanguageFilter?.languageOptions?.none { it.filterKey == "en" } != false) {
                    restoredLanguageFilter = awaitItem().languageFilter
                }
                // Verify English does not silently become selected again after returning.
                assertThat(restoredLanguageFilter.selectedCount).isEqualTo(0)
                assertThat(restoredLanguageFilter.languageOptions.single { it.filterKey == "en" }.selected).isFalse()
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

    private fun createSearchHistoryManager(): SearchHistoryManager {
        return SearchHistoryManager(InMemorySearchRepository())
    }

    private fun createViewModel(
        searchHistoryManager: SearchHistoryManager = createSearchHistoryManager(),
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
        searchFilters: List<SearchFilter> = emptyList(),
        resourceResolving: ResourceResolving = FakeLanguageResourceResolver,
        languageFiltersState: SearchLanguageFiltersState = SearchLanguageFiltersState(),
    ): SearchViewModel {
        return SearchViewModel(
            repository = createRepository(sessionsFlow),
            searchQueryFilter = SearchQueryFilter(),
            searchHistoryManager = searchHistoryManager,
            searchResultParameterFactory = FakeSearchResultParameterFactory(),
            languageFiltersState = languageFiltersState,
            languageFilterUiStateFactory = SearchLanguageFilterUiState.Factory(resourceResolving),
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
                languages = SearchResultProperty("", ""),
                roomName = SearchResultProperty("", ""),
                startsAt = SearchResultProperty(startTime, startTime),
                endsAt = SearchResultProperty("", ""),
                recordingOptOut = null,
            )
        }
    }
}
