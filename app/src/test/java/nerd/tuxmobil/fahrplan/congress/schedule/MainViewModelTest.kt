package nerd.tuxmobil.fahrplan.congress.schedule

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.applinks.Slug
import nerd.tuxmobil.fahrplan.congress.applinks.SlugFactory
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.NEW
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticProperty
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticsUiState
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticsUiStateFactory
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.SimpleMessage
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.TitledMessage
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel

@ExtendWith(MainDispatcherTestExtension::class)
class MainViewModelTest {

    @Test
    fun `initialization does not affect properties`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.loadScheduleUiState.test {
            expectNoEvents()
        }
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.showAbout.test {
            expectNoEvents()
        }
        viewModel.openSessionDetails.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `FetchFailure posts to loadScheduleUiState and errorMessage properties when the user triggered the action`() =
        runTest {
            val status = FetchFailure(HttpStatus.HTTP_DNS_FAILURE, "localhost", "some-error", isUserRequest = true)
            val repository = createRepository(loadScheduleStateFlow = flowOf(status))
            val errorMessageFactory = mock<ErrorMessage.Factory> {
                on { getMessageForHttpStatus(any(), any()) } doReturn TitledMessage("some title", "some message")
            }
            val viewModel = createViewModel(repository, errorMessageFactory = errorMessageFactory)
            val expectedErrorMessage = TitledMessage("some title", "some message")
            viewModel.loadScheduleUiState.test {
                assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.UserTriggeredFetchFailure)
            }
            viewModel.changeStatisticsUiState.test {
                assertThat(awaitItem()).isNull()
            }
            viewModel.openSessionChanges.test {
                expectNoEvents()
            }
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isEqualTo(expectedErrorMessage)
            }
            viewModel.simpleErrorMessageUiState.test {
                expectNoEvents()
            }
            verifyInvokedOnce(repository).loadScheduleState
        }

    @Test
    fun `FetchFailure posts to loadScheduleUiState and errorMessage properties when the user triggered the action and certificate error occurs`() =
        runTest {
            val status = FetchFailure(HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE, "localhost", "some-error", isUserRequest = true)
            val repository = createRepository(loadScheduleStateFlow = flowOf(status))
            val errorMessageFactory = mock<ErrorMessage.Factory> {
                on { getCertificateMessage(any()) } doReturn TitledMessage("Certificate error", "Some certificate error.")
            }
            val viewModel = createViewModel(repository, errorMessageFactory = errorMessageFactory)
            val expectedErrorMessage = TitledMessage("Certificate error", "Some certificate error.")
            viewModel.loadScheduleUiState.test {
                assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.UserTriggeredFetchFailure)
            }
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isEqualTo(expectedErrorMessage)
            }
            viewModel.simpleErrorMessageUiState.test {
                expectNoEvents()
            }
            verifyInvokedOnce(repository).loadScheduleState
        }

    @Test
    fun `onCloseErrorMessageScreen posts null to errorMessage property`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.onCloseErrorMessageScreen()
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
            expectNoEvents()
        }
    }

    @Test
    fun `FetchFailure silently posts to loadScheduleUiState property when the user did not trigger the action`() =
        runTest {
            val status = FetchFailure(HttpStatus.HTTP_DNS_FAILURE, "localhost", "some-error", isUserRequest = false)
            val repository = createRepository(loadScheduleStateFlow = flowOf(status))
            val viewModel = createViewModel(repository)
            viewModel.loadScheduleUiState.test {
                assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.SilentFetchFailure)
            }
            viewModel.changeStatisticsUiState.test {
                assertThat(awaitItem()).isNull()
            }
            viewModel.openSessionChanges.test {
                expectNoEvents()
            }
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
            }
            viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
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
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.simpleErrorMessageUiState.test {
            assertThat(awaitItem()).isNull()
        }
        verifyInvokedOnce(repository).loadScheduleState
    }

    @Test
    fun `ParseSuccess posts to loadScheduleUiState and to changeStatisticsUiState properties`() =
        runTest {
            val repository = createRepository(
                loadScheduleStateFlow = flowOf(ParseSuccess),
                scheduleChangesSeen = false,
                changedSessions = listOf(SessionDatabaseModel(sessionId = "changed-01", changedIsNew = true))
            )
            val viewModel = createViewModel(
                repository,
                changeStatisticsUiStateFactory = createChangeStatisticsUiStateFactory(createChangeStatisticsUiState()),
            )
            viewModel.loadScheduleUiState.test {
                assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Success.ParseSuccess)
            }
            viewModel.changeStatisticsUiState.test {
                assertThat(awaitItem()).isEqualTo(createChangeStatisticsUiState())
            }
            viewModel.openSessionChanges.test {
                expectNoEvents()
            }
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
            }
            viewModel.simpleErrorMessageUiState.test {
                assertThat(awaitItem()).isNull()
            }
            verifyInvokedOnce(repository).loadScheduleState
        }

    @Test
    fun `ParseFailure posts to loadScheduleUiState and simpleErrorMessageUiState properties`() = runTest {
        val parseResult = TestParseResult()
        val repository = createRepository(loadScheduleStateFlow = flowOf(ParseFailure(parseResult)))
        val errorMessageFactory = mock<ErrorMessage.Factory> {
            on { getMessageForParsingResult(any()) } doReturn TitledMessage("Connection failure", "Couldn't parse response.")
        }
        val viewModel = createViewModel(repository, errorMessageFactory = errorMessageFactory)
        val expectedErrorMessage = TitledMessage("Connection failure", "Couldn't parse response.")
        viewModel.loadScheduleUiState.test {
            assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Failure.ParseFailure)
        }
        viewModel.changeStatisticsUiState.test {
            assertThat(awaitItem()).isNull()
        }
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        viewModel.errorMessage.test {
            assertThat(awaitItem()).isEqualTo(expectedErrorMessage)
        }
        viewModel.simpleErrorMessageUiState.test {
            expectNoEvents()
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
            assertThat(awaitItem()).isEqualTo(Unit)
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
    fun `openSessionDetailsFromAppLink posts to openSessionDetails property`() = runTest {
        val repository = createRepository(updatedSelectedSessionId = true)
        val slugFactory = mock<SlugFactory> {
            on { getSlug(any()) } doReturn Slug.PretalxSlug("pretalx-slug")
        }
        val viewModel = createViewModel(repository, slugFactory = slugFactory)
        viewModel.openSessionDetailsFromAppLink(mock())
        viewModel.openSessionDetails.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `openSessionDetailsFromAppLink does not post to openSessionDetails property when slug is null`() = runTest {
        val repository = createRepository(updatedSelectedSessionId = true)
        val slugFactory = mock<SlugFactory> {
            on { getSlug(any()) } doReturn null
        }
        val viewModel = createViewModel(repository, slugFactory = slugFactory)
        viewModel.openSessionDetailsFromAppLink(mock())
        viewModel.openSessionDetails.test {
            expectNoEvents()
        }
    }

    @Test
    fun `openSessionDetailsFromAppLink does not post to openSessionDetails property when slug is not present`() = runTest {
        val repository = createRepository(updatedSelectedSessionId = false)
        val slugFactory = mock<SlugFactory> {
            on { getSlug(any()) } doReturn Slug.PretalxSlug("pretalx-slug")
        }
        val viewModel = createViewModel(repository, slugFactory = slugFactory)
        viewModel.openSessionDetailsFromAppLink(mock())
        viewModel.openSessionDetails.test {
            expectNoEvents()
        }
    }

    @Test
    fun `onCloseChangeStatisticsScreen post to openSessionChanges property`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.onCloseChangeStatisticsScreen(shouldOpenSessionChanges = true)
        viewModel.openSessionChanges.test {
            assertThat(awaitItem()).isEqualTo(Unit)
            expectNoEvents()
        }
        verifyInvokedOnce(repository).updateScheduleChangesSeen(true)
    }

    @Test
    fun `onCloseChangeStatisticsScreen does not post to openSessionChanges property`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.onCloseChangeStatisticsScreen(shouldOpenSessionChanges = false)
        viewModel.openSessionChanges.test {
            expectNoEvents()
        }
        verifyInvokedOnce(repository).updateScheduleChangesSeen(true)
    }

    @Test
    fun `checkPostNotificationsPermission does not post to missingPostNotificationsPermission property (no alarms)`() =
        runTest {
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
    fun `checkPostNotificationsPermission does not post to missingPostNotificationsPermission property (notifications enabled)`() =
        runTest {
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
    fun `checkPostNotificationsPermission posts to missingPostNotificationsPermission property`() =
        runTest {
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
        changedSessions: List<SessionDatabaseModel> = emptyList(),
        updatedSelectedSessionId: Boolean = false,
        alarms: List<Alarm> = emptyList()
    ) = mock<AppRepository> {
        on { loadScheduleState } doReturn loadScheduleStateFlow
        on { readScheduleChangesSeen() } doReturn scheduleChangesSeen
        on { readMeta() } doReturn Meta(version = "")
        on { loadChangedSessions() } doReturn changedSessions
        on { updateSelectedSessionId(any()) } doReturn updatedSelectedSessionId
        on { updateSelectedSessionIdFromSlug(any()) } doReturn updatedSelectedSessionId
        on { readAlarms(any()) } doReturn alarms
        on { readShowScheduleUpdateDialogEnabled() } doReturn true
    }

    private fun createViewModel(
        repository: AppRepository,
        notificationHelper: NotificationHelper = mock(),
        changeStatisticsUiStateFactory: ChangeStatisticsUiStateFactory = mock(),
        errorMessageFactory: ErrorMessage.Factory = createFakeErrorMessageFactory(),
        slugFactory: SlugFactory = mock(),
    ) = MainViewModel(
        repository = repository,
        notificationHelper = notificationHelper,
        changeStatisticsUiStateFactory = changeStatisticsUiStateFactory,
        errorMessageFactory = errorMessageFactory,
        slugFactory = slugFactory,
        executionContext = TestExecutionContext,
    )

    private fun createFakeErrorMessageFactory() = mock<ErrorMessage.Factory> {
        on { getMessageForHttpStatus(any(), any()) } doReturn SimpleMessage("fake message")
        on { getMessageForParsingResult(any()) } doReturn SimpleMessage("fake message")
    }

    private fun createChangeStatisticsUiStateFactory(uiState: ChangeStatisticsUiState) =
        mock<ChangeStatisticsUiStateFactory> {
            on { createChangeStatisticsUiState(any(), any(), any()) } doReturn uiState
        }

    private fun createChangeStatisticsUiState(): ChangeStatisticsUiState {
        val properties = listOf(
            ChangeStatisticProperty(1, CHANGED),
            ChangeStatisticProperty(2, NEW),
            ChangeStatisticProperty(3, CANCELED),
        )
        return ChangeStatisticsUiState("", properties, 10)
    }


}
