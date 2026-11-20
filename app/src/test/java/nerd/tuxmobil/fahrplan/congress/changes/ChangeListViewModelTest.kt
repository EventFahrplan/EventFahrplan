package nerd.tuxmobil.fahrplan.congress.changes

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.UNCHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.CancelScheduleUpdateNotification
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnFavoritesFilterToggle
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnScheduleChangesSeen
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.None
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.preferences.Settings
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class ChangeListViewModelTest {

    @Nested
    inner class SessionChangesState {

        @Test
        fun `sessionChangesState initially emits Loading state`() = runTest {
            val repository = createRepository(changedSessions = emptyFlow())
            val viewModel = createViewModel(repository)
            viewModel.sessionChangesState.test {
                assertThat(awaitItem()).isEqualTo(Loading)
                expectNoEvents()
            }
            verifyInvokedNever(repository).readMeta()
            verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
        }

        @Test
        fun `sessionChangesState emits Success state with empty list`() = runTest {
            val repository = createRepository(changedSessions = flowOf(emptyList()))
            val viewModel = createViewModel(repository, factory = createFactory())
            val expected = Success(emptyList(), favoritesFilterEnabled = false, hasUnfilteredChanges = false)
            viewModel.sessionChangesState.test {
                assertThat(awaitItem()).isEqualTo(expected)
                expectNoEvents()
            }
        }

        @Test
        fun `sessionChangesState emits Success state with converted sessions`() = runTest {
            val session = Session(
                sessionId = "18",
                title = "Title",
                subtitle = "Subtitle",
                recordingOptOut = false,
                speakers = listOf("Jane Doe", "John Doe"),
                dateUTC = 1439478900000L,
                duration = Duration.ofMinutes(30),
                roomName = "Main room",
                language = "de, en",
                dayIndex = 0,
                isFavorite = true,
            )
            val sessions = listOf(session)
            val repository = createRepository(
                changedSessions = flowOf(sessions),
                meta = Meta(numDays = 1),
                useDeviceTimeZoneEnabled = true
            )
            val viewModel = createViewModel(repository, factory = createFactory())
            val expectedSessionChangeParameter = SessionChangeParameter.SessionChange(
                id = "18",
                title = SessionChangeProperty(
                    value = "Title",
                    contentDescription = "",
                    changeType = UNCHANGED
                ),
                subtitle = SessionChangeProperty(
                    value = "Subtitle",
                    contentDescription = "",
                    changeType = UNCHANGED
                ),
                speakerNames = SessionChangeProperty(
                    value = "Jane Doe, John Doe",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                roomName = SessionChangeProperty(
                    value = "Main room",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                dayText = SessionChangeProperty(
                    value = "",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                startsAt = SessionChangeProperty(
                    value = "",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                duration = SessionChangeProperty(
                    value = "30 min",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                languages = SessionChangeProperty(
                    value = "de, en",
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                videoRecordingState = SessionChangeProperty(
                    value = None,
                    contentDescription = "",
                    changeType = UNCHANGED,
                ),
                isCanceled = false,
            )

            val expectedState = Success(
                sessionChangeParameters = listOf(expectedSessionChangeParameter),
                favoritesFilterEnabled = false,
                hasUnfilteredChanges = true,
            )
            viewModel.sessionChangesState.test {
                assertThat(awaitItem()).isEqualTo(expectedState)
                expectNoEvents()
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

        @Test
        fun `sessionChangesState emits all sessions by default`() = runTest {
            val sessions = listOf(createSession("1", isFavorite = true), createSession("2", isFavorite = false))
            val repository = createRepository(changedSessions = flowOf(sessions))
            val viewModel = createViewModel(repository, factory = createFactory())
            viewModel.sessionChangesState.test {
                val state = awaitItem() as Success
                assertThat(state.sessionChangeParameters.ids()).containsExactly("1", "2").inOrder()
                assertThat(state.favoritesFilterEnabled).isFalse()
                assertThat(state.hasUnfilteredChanges).isTrue()
                expectNoEvents()
            }
        }

        @Test
        fun `sessionChangesState emits only favorite sessions if favorites filter is persisted as enabled`() = runTest {
            val sessions = listOf(createSession("1", isFavorite = true), createSession("2", isFavorite = false))
            val repository = createRepository(changedSessions = flowOf(sessions))
            val settingsRepository = createSettingsRepository(favoritesFilterEnabled = true)
            val viewModel = createViewModel(repository, settingsRepository, factory = createFactory())
            viewModel.sessionChangesState.test {
                val state = awaitItem() as Success
                assertThat(state.sessionChangeParameters.ids()).containsExactly("1")
                assertThat(state.favoritesFilterEnabled).isTrue()
                expectNoEvents()
            }
        }

        @Test
        fun `sessionChangesState emits empty list but hasUnfilteredChanges if favorites filter is enabled and no favorite changed`() = runTest {
            val sessions = listOf(createSession("2", isFavorite = false))
            val repository = createRepository(changedSessions = flowOf(sessions))
            val settingsRepository = createSettingsRepository(favoritesFilterEnabled = true)
            val viewModel = createViewModel(repository, settingsRepository, factory = createFactory())
            val expected = Success(emptyList(), favoritesFilterEnabled = true, hasUnfilteredChanges = true)
            viewModel.sessionChangesState.test {
                assertThat(awaitItem()).isEqualTo(expected)
                expectNoEvents()
            }
        }

        @Test
        fun `sessionChangesState emits favorite sessions when favorites filter gets enabled and all ones when disabled again`() = runTest {
            val sessions = listOf(createSession("1", isFavorite = true), createSession("2", isFavorite = false))
            val repository = createRepository(changedSessions = flowOf(sessions))
            val settingsRepository = createSettingsRepository()
            val viewModel = createViewModel(repository, settingsRepository, factory = createFactory())
            viewModel.sessionChangesState.test {
                assertThat((awaitItem() as Success).sessionChangeParameters.ids()).containsExactly("1", "2").inOrder()

                viewModel.onViewEvent(OnFavoritesFilterToggle(enabled = true))
                val filtered = awaitItem() as Success
                assertThat(filtered.sessionChangeParameters.ids()).containsExactly("1")
                assertThat(filtered.favoritesFilterEnabled).isTrue()

                viewModel.onViewEvent(OnFavoritesFilterToggle(enabled = false))
                val unfiltered = awaitItem() as Success
                assertThat(unfiltered.sessionChangeParameters.ids()).containsExactly("1", "2").inOrder()
                assertThat(unfiltered.favoritesFilterEnabled).isFalse()
                expectNoEvents()
            }
            verifyInvokedOnce(settingsRepository).setScheduleChangesFavoritesFilter(true)
            verifyInvokedOnce(settingsRepository).setScheduleChangesFavoritesFilter(false)
        }

    }

    @Nested
    inner class OnViewEvent {

        @Test
        fun `OnSessionChangeItemClick emits NavigateToSession effect`() = runTest {
            val viewModel = createViewModel(createRepository())
            viewModel.onViewEvent(OnSessionChangeItemClick(sessionId = "42"))
            viewModel.effects.test {
                assertThat(awaitItem()).isEqualTo(NavigateToSession("42"))
            }
        }

        @Test
        fun `OnScheduleChangesSeen invokes updateScheduleChangesSeen and emits CancelScheduleUpdateNotification effect`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnScheduleChangesSeen)
            viewModel.effects.test {
                assertThat(awaitItem()).isEqualTo(CancelScheduleUpdateNotification)
            }
            verifyInvokedOnce(repository).updateScheduleChangesSeen(changesSeen = true)
        }

    }

    private fun List<SessionChangeParameter>.ids() =
        filterIsInstance<SessionChangeParameter.SessionChange>().map { it.id }

    private fun createSession(sessionId: String, isFavorite: Boolean) = Session(
        sessionId = sessionId,
        isFavorite = isFavorite,
    )

    private fun createFactory(): SessionChangeParametersFactory = mock<SessionChangeParametersFactory> {
        on { createSessionChangeParameters(anyOrNull(), anyOrNull()) } doAnswer { invocation ->
            invocation.getArgument<List<Session>>(0).map(::toSessionChangeParameter)
        }
    }

    private fun toSessionChangeParameter(session: Session) = SessionChangeParameter.SessionChange(
        id = session.sessionId,
        title = SessionChangeProperty(
            value = session.title,
            contentDescription = "",
            changeType = UNCHANGED
        ),
        subtitle = SessionChangeProperty(
            value = session.subtitle,
            contentDescription = "",
            changeType = UNCHANGED
        ),
        speakerNames = SessionChangeProperty(
            value = session.speakers.joinToString(),
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        roomName = SessionChangeProperty(
            value = session.roomName,
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        dayText = SessionChangeProperty(
            value = "",
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        startsAt = SessionChangeProperty(
            value = "",
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        duration = SessionChangeProperty(
            value = "${session.duration.toWholeMinutes()} min",
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        languages = SessionChangeProperty(
            value = session.language,
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        videoRecordingState = SessionChangeProperty(
            value = None,
            contentDescription = "",
            changeType = UNCHANGED,
        ),
        isCanceled = false,
    )

    private fun createRepository(
        changedSessions: Flow<List<Session>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0),
        useDeviceTimeZoneEnabled: Boolean = false,
    ) = mock<AppRepository> {
        on { this.changedSessions } doReturn changedSessions
        on { this.readMeta() } doReturn meta
        on { this.readUseDeviceTimeZoneEnabled() } doReturn useDeviceTimeZoneEnabled
    }

    private fun createSettingsRepository(
        favoritesFilterEnabled: Boolean = false,
    ) = mock<SettingsRepository> {
        // Mimics the setting being persisted and emitted again.
        val settingsStream = MutableStateFlow(Settings(isScheduleChangesFavoritesFilterEnabled = favoritesFilterEnabled))
        on { this.settingsStream } doReturn settingsStream
        on { this.setScheduleChangesFavoritesFilter(any()) } doAnswer { invocation ->
            settingsStream.value = settingsStream.value.copy(isScheduleChangesFavoritesFilterEnabled = invocation.getArgument(0))
        }
    }

    private fun createViewModel(
        repository: AppRepository,
        settingsRepository: SettingsRepository = createSettingsRepository(),
        factory: SessionChangeParametersFactory = mock(),
    ) = ChangeListViewModel(
        repository = repository,
        settingsRepository = settingsRepository,
        executionContext = TestExecutionContext,
        sessionChangeParametersFactory = factory,
    )

}
