package nerd.tuxmobil.fahrplan.congress.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Success
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class SearchViewModelTest {

    @Nested
    inner class SearchResultsState {

        @Test
        fun `searchResultsState emits Loading state`() = runTest {
            val repository = createRepository(emptyFlow())
            val viewModel = createViewModel(repository)
            viewModel.searchResultsState.test {
                assertThat(awaitItem()).isEqualTo(Loading)
                expectNoEvents()
            }
        }

        @Test
        fun `searchResultsState emits Success state with empty list`() = runTest {
            val repository = createRepository(flowOf(emptyList()))
            val viewModel = createViewModel(repository)
            viewModel.searchResultsState.test {
                assertThat(awaitItem()).isEqualTo(Success(emptyList()))
                expectNoEvents()
            }
        }

        @Test
        fun `searchResultsState emits Success state with list of search results`() = runTest {
            val repository = createRepository()
            val actual = listOf(
                SearchResult(
                    id = "1",
                    title = SearchResultProperty("Title", ""),
                    speakerNames = SearchResultProperty("Speakers", ""),
                    startsAt = SearchResultProperty("10:30", ""),
                )
            )
            val viewModel = createViewModel(repository = repository, parameters = actual)
            viewModel.onViewEvent(OnSearchQueryChange("title"))
            val expected = listOf(
                SearchResult(
                    id = "1",
                    title = SearchResultProperty("Title", ""),
                    speakerNames = SearchResultProperty("Speakers", ""),
                    startsAt = SearchResultProperty("10:30", ""),
                )
            )
            viewModel.searchResultsState.test {
                assertThat(awaitItem()).isEqualTo(Success(expected))
                expectNoEvents()
            }
        }

    }

    @Nested
    inner class Navigation {

        @Test
        fun `navigateBack emits when OnBackPress event is received`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnBackPress)
            viewModel.navigateBack.test {
                assertThat(awaitItem()).isEqualTo(Unit)
                expectNoEvents()
            }
        }

        @Test
        fun `navigates to session details when OnSearchResultItemClick event is received`() =
            runTest {
                val repository = createRepository()
                val viewModel = createViewModel(repository)
                val screenNavigation = mock<ScreenNavigation>()
                viewModel.screenNavigation = screenNavigation
                viewModel.onViewEvent(OnSearchResultItemClick("42"))
                verifyInvokedOnce(screenNavigation).navigateToSessionDetails("42")
            }

        @Test
        fun `does not navigate to session details when screen navigation is not set`() =
            runTest {
                val repository = createRepository()
                val viewModel = createViewModel(repository)
                val screenNavigation = mock<ScreenNavigation>()
                viewModel.screenNavigation = null
                viewModel.onViewEvent(OnSearchResultItemClick("42"))
                verifyInvokedNever(screenNavigation).navigateToSessionDetails("42")
            }

        @Test
        fun `searchQuery is empty string when OnBackIconClick event is received`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnBackIconClick)
            assertThat(viewModel.searchQuery).isEmpty()
        }

        @Test
        fun `searchQuery is empty string when OnSearchSubScreenBackPress event is received`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSearchSubScreenBackPress)
            assertThat(viewModel.searchQuery).isEmpty()
        }

    }

    @Nested
    inner class Query {

        @Test
        fun `searchQuery matches passed string when OnSearchQueryChange event is received`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSearchQueryChange("query"))
            assertThat(viewModel.searchQuery).isEqualTo("query")
        }

        @Test
        fun `searchQuery is empty string when OnSearchQueryClear event is received`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSearchQueryClear)
            assertThat(viewModel.searchQuery).isEmpty()
        }

    }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
    ) = mock<AppRepository> {
        on { this.sessions } doReturn sessionsFlow
    }

    private fun createViewModel(
        repository: AppRepository,
        parameters: List<SearchResult> = emptyList(),
    ) = SearchViewModel(
        repository = repository,
        searchQueryFilter = mock(), // overlayed by factory
        searchResultParameterFactory = createSearchResultParameterFactory(parameters),
    )

    private fun createSearchResultParameterFactory(
        parameters: List<SearchResult>,
    ) = mock<SearchResultParameterFactory> {
        on { createSearchResults(any(), any()) } doReturn parameters
    }

}
