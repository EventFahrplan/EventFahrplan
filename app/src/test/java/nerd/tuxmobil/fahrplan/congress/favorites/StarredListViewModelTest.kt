package nerd.tuxmobil.fahrplan.congress.favorites

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListDestination.ConfirmDeleteAll
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Loading
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Success
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnDeleteSelectedClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnItemLongClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnSelectionModeDismiss
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameterFactory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultProperty
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class StarredListViewModelTest {

    private val simpleSessionFormat = mock<SimpleSessionFormat>()
    private val jsonSessionFormat = mock<JsonSessionFormat>()

    @Nested
    inner class UiState {

        @Test
        fun `uiState emits Loading`() = runTest {
            val repository = createRepository(sessions = emptyFlow())
            val viewModel = createViewModel(repository)
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(Loading)
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

        @Test
        fun `uiState emits Success with zero sessions`() = runTest {
            val repository = createRepository(sessions = flowOf(emptyList()))
            val viewModel = createViewModel(repository)
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(Success(emptyList()))
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

        @Test
        fun `uiState emits Success with single session`() = runTest {
            val repository = createRepository(
                sessions = flowOf(listOf(Session("23"))),
                meta = Meta(numDays = 2),
                useDeviceTimeZoneEnabled = true,
            )
            val viewModel = createViewModel(repository)
            val expected = SearchResult(
                id = "23",
                title = SearchResultProperty("", ""),
                speakerNames = SearchResultProperty("", ""),
                languages = SearchResultProperty("", ""),
                roomName = SearchResultProperty("", ""),
                startsAt = SearchResultProperty("0", "0"),
                endsAt = SearchResultProperty("", ""),
                recordingOptOut = null,
            )
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(Success(listOf(expected)))
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

    }

    @Nested
    inner class SelectedIds {

        @Test
        fun `selectedIds emits empty set`() = runTest {
            val repository = createRepository(sessions = emptyFlow())
            val viewModel = createViewModel(repository)
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

        @Test
        fun `selectedIds emits empty set when sessions are empty`() = runTest {
            val repository = createRepository(sessions = flowOf(emptyList()))
            val viewModel = createViewModel(repository)
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

        @Test
        fun `selectedIds emits set with single sessionId`() = runTest {
            val repository = createRepository(
                sessions = flowOf(listOf(Session("23"))),
                meta = Meta(numDays = 2),
                useDeviceTimeZoneEnabled = true,
            )
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnItemLongClick("23"))
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEqualTo(setOf("23"))
            }
        }

    }

    @Nested
    inner class HasStarredSessions {

        @Test
        fun `hasStarredSessions does not emit`() = runTest {
            val repository = createRepository(sessions = emptyFlow())
            val viewModel = createViewModel(repository)
            viewModel.hasStarredSessions.test {
                awaitComplete()
            }
        }

        @Test
        fun `hasStarredSessions emits false`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.hasStarredSessions.test {
                assertThat(awaitItem()).isFalse()
                awaitComplete()
            }
        }

        @Test
        fun `hasStarredSessions returns a single session`() = runTest {
            val repository = createRepository(
                sessions = flowOf(listOf(Session("23"))),
                meta = Meta(numDays = 2),
                useDeviceTimeZoneEnabled = true,
            )
            val viewModel = createViewModel(repository)
            viewModel.hasStarredSessions.test {
                assertThat(awaitItem()).isTrue()
                awaitComplete()
            }
        }

    }

    @Nested
    inner class ShareSimple {

        @Test
        fun `initialization does not emit ShareSimple effect`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.effects.test {
                expectNoEvents()
            }
        }

        @Test
        fun `share emits ShareSimple effect when session is present`() = runTest {
            val repository = createRepository(
                sessions = flowOf(listOf(Session("23"))),
                meta = Meta(numDays = 0, timeZoneId = null),
            )
            val fakeSessionFormat = mock<SimpleSessionFormat> {
                on { format(any<List<Session>>(), anyOrNull()) } doReturn "session-23"
            }
            val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
            viewModel.effects.test {
                viewModel.share()
                assertThat(awaitItem()).isEqualTo(ShareSimple("session-23"))
            }
        }

        @Test
        fun `share never emits ShareSimple effect when sessions is empty`() = runTest {
            val repository = createRepository(
                sessions = flowOf(emptyList()),
                meta = Meta(numDays = 0, timeZoneId = null),
            )
            val fakeSessionFormat = mock<SimpleSessionFormat> {
                on { format(any<List<Session>>(), anyOrNull()) } doReturn null // simulating empty list
            }
            val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
            viewModel.share()
            viewModel.effects.test {
                expectNoEvents()
            }
        }

    }

    @Nested
    inner class ShareJson {

        @Test
        fun `initialization does not emit ShareJson effect`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.effects.test {
                expectNoEvents()
            }
        }

        @Test
        fun `shareToChaosflix emits ShareJson effect when session is present`() = runTest {
            val repository = createRepository(sessions = flowOf(listOf(Session("17"))))
            val fakeSessionFormat = mock<JsonSessionFormat> {
                on { format(any<List<Session>>()) } doReturn "session-17"
            }
            val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
            viewModel.effects.test {
                viewModel.shareToChaosflix()
                assertThat(awaitItem()).isEqualTo(ShareJson("session-17"))
            }
        }

        @Test
        fun `shareToChaosflix never emits ShareJson effect when sessions is empty`() = runTest {
            val repository = createRepository(sessions = flowOf(emptyList()))
            val fakeSessionFormat = mock<JsonSessionFormat> {
                on { format(any<List<Session>>()) } doReturn null // simulating empty list
            }
            val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
            viewModel.shareToChaosflix()
            viewModel.effects.test {
                expectNoEvents()
            }
        }

    }

    @Nested
    inner class OnViewEvent {

        @Test
        fun `OnItemClick emits NavigateToSession effect`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.effects.test {
                viewModel.onViewEvent(OnItemClick("42"))
                assertThat(awaitItem()).isEqualTo(NavigateToSession("42"))
            }
        }

        @Test
        fun `OnDeleteAllWithConfirmationClick emits ConfirmDeleteAll effect`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.effects.test {
                viewModel.onViewEvent(OnDeleteAllWithConfirmationClick)
                assertThat(awaitItem()).isEqualTo(NavigateTo(ConfirmDeleteAll))
            }
        }

        @Test
        fun `OnDeleteAllClick invokes deleteAllHighlights`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnDeleteAllClick)
            verifyInvokedOnce(repository).deleteAllHighlights()
        }

        @Test
        fun `OnItemLongClick toggles selection`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEmpty()

                viewModel.onViewEvent(OnItemLongClick("1"))
                assertThat(awaitItem()).containsExactly("1")

                viewModel.onViewEvent(OnItemLongClick("1"))
                assertThat(awaitItem()).isEmpty()
            }
        }

        @Test
        fun `OnSelectionModeDismiss clears selection`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnItemLongClick("1"))
            viewModel.onViewEvent(OnItemLongClick("2"))
            viewModel.onViewEvent(OnSelectionModeDismiss)
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEmpty()
            }
        }

        @Test
        fun `OnDeleteSelectedClick unfavors selected sessions and clears selection`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnItemLongClick("1"))
            viewModel.onViewEvent(OnItemLongClick("2"))
            viewModel.onViewEvent(OnDeleteSelectedClick)
            viewModel.selectedIds.test {
                assertThat(awaitItem()).isEmpty()
            }
            verifyInvokedOnce(repository).deleteHighlights("1", "2")
        }

    }

    private fun createRepository(
        sessions: Flow<List<Session>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0),
        useDeviceTimeZoneEnabled: Boolean = false
    ) = mock<AppRepository> {
        on { starredSessions } doReturn sessions
        on { readMeta() } doReturn meta
        on { readUseDeviceTimeZoneEnabled() } doReturn useDeviceTimeZoneEnabled
    }

    private fun createViewModel(
        repository: AppRepository,
        simpleSessionFormat: SimpleSessionFormat = this.simpleSessionFormat,
        jsonSessionFormat: JsonSessionFormat = this.jsonSessionFormat,
        searchResultParameterFactory: SearchResultParameterFactory = FakeSearchResultParameterFactory()
    ) = StarredListViewModel(
        repository,
        TestExecutionContext,
        simpleSessionFormat,
        jsonSessionFormat,
        searchResultParameterFactory
    )

}

private class FakeSearchResultParameterFactory : SearchResultParameterFactory {

    override fun createSearchResults(
        sessions: List<Session>,
        useDeviceTimeZone: Boolean
    ) = sessions.map { session ->
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
