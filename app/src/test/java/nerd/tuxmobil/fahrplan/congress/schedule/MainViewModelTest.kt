package nerd.tuxmobil.fahrplan.congress.schedule

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScheduleChangesParameter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class MainViewModelTest {

    private val logging = NoLogging

    @Test
    fun `initialization does not affect properties`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            expectNoEvents()
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.showAbout.test {
            expectNoEvents()
        }
        viewModel.openSessionDetails.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            expectNoEvents()
        }
        viewModel.parseFailure.test {
            expectNoEvents()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `InitialFetching posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(InitialFetching))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Initializing.InitialFetching)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `Fetching posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(Fetching))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Active.Fetching)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `FetchSuccess posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(FetchSuccess))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Success.FetchSuccess)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `FetchFailure posts to loadScheduleUiState and fetchFailure properties when the user triggered the action`() = runTest {
        val status = FetchFailure(HttpStatus.HTTP_DNS_FAILURE, "localhost", "some-error", isUserRequest = true)
        val repository = createRepository(loadScheduleStateFlow = flowOf(status))
        val viewModel = createViewModel(repository)
        val expectedFailure = FetchFailure(HttpStatus.HTTP_DNS_FAILURE, "localhost", "some-error", isUserRequest = true)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.UserTriggeredFetchFailure)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isEqualTo(expectedFailure)
        }
        viewModel.parseFailure.test {
            expectNoEvents()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `FetchFailure silently posts to loadScheduleUiState property when the user did not trigger the action`() = runTest {
        val status = FetchFailure(HttpStatus.HTTP_DNS_FAILURE, "localhost", "some-error", isUserRequest = false)
        val repository = createRepository(loadScheduleStateFlow = flowOf(status))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.SilentFetchFailure)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            expectNoEvents()
        }
        viewModel.parseFailure.test {
            expectNoEvents()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `InitialParsing posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(InitialParsing))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Initializing.InitialParsing)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `Parsing posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(Parsing))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Active.Parsing)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `ParseSuccess posts to loadScheduleUiState property`() = runTest {
        val repository = createRepository(
            loadScheduleStateFlow = flowOf(ParseSuccess),
            scheduleChangesSeen = true
        )
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Success.ParseSuccess)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `ParseSuccess posts to loadScheduleUiState and to scheduleChangesParameter properties`() = runTest {
        val repository = createRepository(
            loadScheduleStateFlow = flowOf(ParseSuccess),
            scheduleChangesSeen = false,
            changedSessions = listOf(Session("changed-01").apply { changedIsNew = true })
        )
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Success.ParseSuccess)
        }
        val expectedSessions = listOf(Session("changed-01").apply { changedIsNew = true })
        val expectedChangeStatistic = ChangeStatistic.of(expectedSessions, logging)
        val expectedScheduleChangesParameter = ScheduleChangesParameter(scheduleVersion = "", expectedChangeStatistic)
        viewModel.scheduleChangesParameter.test {
            assertThat(awaitItem()).isEqualTo(expectedScheduleChangesParameter)
        }
        viewModel.fetchFailure.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `ParseFailure posts to loadScheduleUiState and parseFailure properties`() = runTest {
        val parseResult = TestParseResult()
        val repository = createRepository(loadScheduleStateFlow = flowOf(ParseFailure(parseResult)))
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.ParseFailure)
        }
        viewModel.scheduleChangesParameter.test {
            expectNoEvents()
        }
        viewModel.fetchFailure.test {
            expectNoEvents()
        }
        viewModel.parseFailure.test {
            assertThat(awaitItem()).isEqualTo(parseResult)
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `requestScheduleUpdate invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.requestScheduleUpdate(isUserRequest = true)
        verifyInvokedOnce(repository).loadSchedule(isUserRequest = true, onFetchingDone = {}, onParsingDone = {}, onLoadingShiftsDone = {})
    }

    @Test
    fun `cancelLoading invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.cancelLoading()
        verifyInvokedOnce(repository).cancelLoading()
    }

    @Test
    fun `deleteSessionAlarmNotificationId invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.deleteSessionAlarmNotificationId(7)
        verifyInvokedOnce(repository).deleteSessionAlarmNotificationId(7)
    }

    @Test
    fun `showAboutDialog posts to showAbout property`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.showAboutDialog()
        viewModel.showAbout.test {
            assertThat(awaitItem()).isEqualTo(Meta(version = ""))
        }
    }

    @Test
    fun `openSessionDetails posts to openSessionDetails property`() = runTest {
        val repository = createRepository(updatedSelectedSessionId = true)
        val viewModel = createViewModel(repository)
        viewModel.openSessionDetails("S1")
        viewModel.openSessionDetails.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `openSessionDetails does not post to openSessionDetails property`() = runTest {
        val repository = createRepository(updatedSelectedSessionId = false)
        val viewModel = createViewModel(repository)
        viewModel.openSessionDetails("S1")
        viewModel.openSessionDetails.test {
            expectNoEvents()
        }
    }

    @Test
    fun `checkPostNotificationsPermission does not post to missingPostNotificationsPermission property (no alarms)`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val repository = createRepository(alarms = emptyList())
        val viewModel = createViewModel(repository, notificationHelper)
        viewModel.checkPostNotificationsPermission()
        viewModel.missingPostNotificationsPermission.test {
            expectNoEvents()
        }
    }

    @Test
    fun `checkPostNotificationsPermission does not post to missingPostNotificationsPermission property (notifications enabled)`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val repository = createRepository(alarms = listOf(mock()))
        val viewModel = createViewModel(repository, notificationHelper)
        viewModel.checkPostNotificationsPermission()
        viewModel.missingPostNotificationsPermission.test {
            expectNoEvents()
        }
    }

    @Test
    fun `checkPostNotificationsPermission posts to missingPostNotificationsPermission property`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn false
        }
        val repository = createRepository(alarms = listOf(mock()))
        val viewModel = createViewModel(repository, notificationHelper)
        viewModel.checkPostNotificationsPermission()
        viewModel.missingPostNotificationsPermission.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    private class TestParseResult(
        override val isSuccess: Boolean = false
    ) : ParseResult

    private fun createRepository(
        loadScheduleStateFlow: Flow<LoadScheduleState> = emptyFlow(),
        scheduleChangesSeen: Boolean = true,
        changedSessions: List<Session> = emptyList(),
        updatedSelectedSessionId: Boolean = false,
        alarms: List<Alarm> = emptyList()
    ) = mock<AppRepository> {
        on { loadScheduleState } doReturn loadScheduleStateFlow
        on { readScheduleChangesSeen() } doReturn scheduleChangesSeen
        on { readMeta() } doReturn Meta(version = "")
        on { loadChangedSessions() } doReturn changedSessions
        on { updateSelectedSessionId(any()) } doReturn updatedSelectedSessionId
        on { readAlarms(any()) } doReturn alarms
    }

    private fun createViewModel(
        repository: AppRepository,
        notificationHelper: NotificationHelper = mock()
    ) = MainViewModel(
        repository = repository,
        notificationHelper = notificationHelper,
        executionContext = TestExecutionContext,
        logging = logging
    )

}
