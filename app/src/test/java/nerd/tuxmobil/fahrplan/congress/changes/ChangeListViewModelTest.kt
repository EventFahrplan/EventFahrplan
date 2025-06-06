package nerd.tuxmobil.fahrplan.congress.changes

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.UNCHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.None
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class ChangeListViewModelTest {

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
        val viewModel = createViewModel(repository, factory = createFactory(emptyList()))
        val expected = Success(emptyList())
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
        )
        val sessions = listOf(session)
        val repository = createRepository(
            changedSessions = flowOf(sessions),
            meta = Meta(numDays = 1),
            useDeviceTimeZoneEnabled = true
        )
        val viewModel = createViewModel(repository, factory = createFactory(sessions))
        val expectedSessionChangeParameter = SessionChangeParameter.SessionChange(
            id = "18",
            title = SessionChangeProperty(
                value = "Title",
                contentDescription = "",
                changeState = UNCHANGED
            ),
            subtitle = SessionChangeProperty(
                value = "Subtitle",
                contentDescription = "",
                changeState = UNCHANGED
            ),
            speakerNames = SessionChangeProperty(
                value = "Jane Doe, John Doe",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            roomName = SessionChangeProperty(
                value = "Main room",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            dayText = SessionChangeProperty(
                value = "",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            startsAt = SessionChangeProperty(
                value = "",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            duration = SessionChangeProperty(
                value = "30 min",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            languages = SessionChangeProperty(
                value = "de, en",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            videoRecordingState = SessionChangeProperty(
                value = None,
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            isCanceled = false,
        )

        val expectedState = Success(listOf(expectedSessionChangeParameter))
        viewModel.sessionChangesState.test {
            assertThat(awaitItem()).isEqualTo(expectedState)
            expectNoEvents()
        }
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `onViewEvent(OnSessionChangeItemClick) invokes navigateToSessionDetails`() = runTest {
        val screenNavigation = mock<ScreenNavigation>()
        val viewModel = createViewModel(createRepository())
        viewModel.screenNavigation = screenNavigation
        viewModel.onViewEvent(OnSessionChangeItemClick(sessionId = "42"))
        verifyInvokedOnce(screenNavigation).navigateToSessionDetails(sessionId = "42")
    }

    @Test
    fun `updateScheduleChangesSeen invokes repository function`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.updateScheduleChangesSeen(changesSeen = true)
        viewModel.scheduleChangesSeen.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
        verifyInvokedOnce(repository).updateScheduleChangesSeen(changesSeen = true)
    }

    private fun createFactory(sessions: List<Session>): SessionChangeParametersFactory {
        val parameters = sessions.map {
            SessionChangeParameter.SessionChange(
                id = it.sessionId,
                title = SessionChangeProperty(
                    value = it.title,
                    contentDescription = "",
                    changeState = UNCHANGED
                ),
                subtitle = SessionChangeProperty(
                    value = it.subtitle,
                    contentDescription = "",
                    changeState = UNCHANGED
                ),
                speakerNames = SessionChangeProperty(
                    value = it.speakers.joinToString(),
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                roomName = SessionChangeProperty(
                    value = it.roomName,
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                dayText = SessionChangeProperty(
                    value = "",
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                startsAt = SessionChangeProperty(
                    value = "",
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                duration = SessionChangeProperty(
                    value = "${it.duration.toWholeMinutes()} min",
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                languages = SessionChangeProperty(
                    value = it.language,
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                videoRecordingState = SessionChangeProperty(
                    value = None,
                    contentDescription = "",
                    changeState = UNCHANGED,
                ),
                isCanceled = false,
            )
        }
        return mock<SessionChangeParametersFactory> {
            on { createSessionChangeParameters(anyOrNull(), anyOrNull(), anyOrNull()) } doReturn parameters
        }
    }

    private fun createRepository(
        changedSessions: Flow<List<Session>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0),
        useDeviceTimeZoneEnabled: Boolean = false
    ) = mock<AppRepository> {
        on { this.changedSessions } doReturn changedSessions
        on { this.readMeta() } doReturn meta
        on { this.readUseDeviceTimeZoneEnabled() } doReturn useDeviceTimeZoneEnabled
    }

    private fun createViewModel(
        repository: AppRepository,
        factory: SessionChangeParametersFactory = mock(),
    ) = ChangeListViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        sessionChangeParametersFactory = factory,
    )

}
