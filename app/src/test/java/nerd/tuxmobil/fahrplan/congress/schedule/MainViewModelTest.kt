package nerd.tuxmobil.fahrplan.congress.schedule

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.commons.testing.withMethod
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
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.HOST_MISSING
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
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
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnAboutClick
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnAppLinkReceived
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnChangeStatisticsFavoritesFilterToggle
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnCheckPostNotificationsPermission
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnCloseChangeStatisticsScreen
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnCloseErrorMessageScreen
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnScheduleUpdateRequest
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnSessionAlarmNotificationTapped
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewEvent.OnSessionItemClick
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
    fun `EngelsystemUriParsingResult Error posts null to errorMessage property when Engelsystem URL parsing succeeded`() =
        runTest {
            val repository = createRepository(
                engelsystemUriParsingErrorStateFlow = flowOf(null)
            )
            val errorMessageFactory = mock<ErrorMessage.Factory> {
                on { getMessageForEngelsystemUrlError(any()) } doReturn TitledMessage("fake message", "fake url")
            }
            val viewModel = createViewModel(repository, errorMessageFactory = errorMessageFactory)
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
            }
            verifyInvokedOnce(repository).engelsystemUriParsingErrorState
        }

    @Test
    fun `EngelsystemUriParsingResult Error posts TitledMessage to errorMessage property when Engelsystem URL parsing failed`() =
        runTest {
            val repository = createRepository(
                engelsystemUriParsingErrorStateFlow = flowOf(Error(HOST_MISSING, "https://?key=a1b2c3"))
            )
            val errorMessageFactory = mock<ErrorMessage.Factory> {
                on { getMessageForEngelsystemUrlError(any()) } doReturn TitledMessage("fake message", "fake url")
            }
            val viewModel = createViewModel(repository, errorMessageFactory = errorMessageFactory)
            val expectedErrorMessage = TitledMessage("fake message", "fake url")
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isEqualTo(expectedErrorMessage)
            }
            verifyInvokedOnce(repository).engelsystemUriParsingErrorState
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
    fun `ParseSuccess creates changeStatisticsUiState from filtered changed sessions only`() =
        runTest {
            val repository = createRepository(
                loadScheduleStateFlow = flowOf(ParseSuccess),
                scheduleChangesSeen = false,
                changedSessions = listOf(
                    SessionDatabaseModel(sessionId = "changed-01", changedIsNew = true),
                    SessionDatabaseModel(sessionId = "changed-02", changedIsCanceled = true, isFavorite = true),
                ),
                filteredChangedSessions = listOf(
                    SessionDatabaseModel(sessionId = "changed-02", changedIsCanceled = true, isFavorite = true),
                ),
            )
            val changeStatisticsUiStateFactory = createChangeStatisticsUiStateFactory(createChangeStatisticsUiState())
            val viewModel = createViewModel(
                repository,
                changeStatisticsUiStateFactory = changeStatisticsUiStateFactory,
            )
            viewModel.changeStatisticsUiState.test {
                assertThat(awaitItem()).isEqualTo(createChangeStatisticsUiState())
            }
            verifyInvokedOnce(changeStatisticsUiStateFactory).createChangeStatisticsUiState(
                argThat { map { it.sessionId } == listOf("changed-02") },
                any(),
                any(),
                any(),
            )
        }

    @Test
    fun `ParseSuccess does not post to changeStatisticsUiState property if all changed sessions are filtered out`() =
        runTest {
            val repository = createRepository(
                loadScheduleStateFlow = flowOf(ParseSuccess),
                scheduleChangesSeen = false,
                changedSessions = listOf(SessionDatabaseModel(sessionId = "changed-01", changedIsNew = true)),
                filteredChangedSessions = emptyList(),
            )
            val changeStatisticsUiStateFactory = createChangeStatisticsUiStateFactory(createChangeStatisticsUiState())
            val viewModel = createViewModel(
                repository,
                changeStatisticsUiStateFactory = changeStatisticsUiStateFactory,
            )
            viewModel.loadScheduleUiState.test {
                assertThat(awaitItem()).isEqualTo(LoadScheduleUiState.Success.ParseSuccess)
            }
            viewModel.changeStatisticsUiState.test {
                assertThat(awaitItem()).isNull()
            }
            verifyInvokedNever(changeStatisticsUiStateFactory).createChangeStatisticsUiState(any(), any(), any(), any())
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
    fun `onCleared invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        MainViewModel::class.withMethod("onCleared") {
            invoke(viewModel)
        }
        verifyInvokedOnce(repository).cancelLoading()
    }

    @Nested
    inner class OnViewEvent {

        @Test
        fun `OnCloseErrorMessageScreen posts null to errorMessage property`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnCloseErrorMessageScreen)
            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
                expectNoEvents()
            }
        }

        @Test
        fun `OnScheduleUpdateRequest invokes repository function`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnScheduleUpdateRequest(isUserRequest = true))
            verifyInvokedOnce(repository).loadSchedule(isUserRequest = true)
        }

        @Test
        fun `OnSessionAlarmNotificationTapped invokes repository function`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSessionAlarmNotificationTapped(7))
            verifyInvokedOnce(repository).deleteSessionAlarmNotificationId(7)
        }

        @Test
        fun `OnAboutClick posts to showAbout property`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnAboutClick)
            viewModel.showAbout.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
        }

        @Test
        fun `OnSessionItemClick posts to openSessionDetails property`() = runTest {
            val repository = createRepository(updatedSelectedSessionId = true)
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSessionItemClick("S1"))
            viewModel.openSessionDetails.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
        }

        @Test
        fun `OnSessionItemClick does not post to openSessionDetails property`() = runTest {
            val repository = createRepository(updatedSelectedSessionId = false)
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnSessionItemClick("S1"))
            viewModel.openSessionDetails.test {
                expectNoEvents()
            }
        }

        @Test
        fun `OnAppLinkReceived posts to openSessionDetails property`() = runTest {
            val repository = createRepository(updatedSelectedSessionId = true)
            val slugFactory = mock<SlugFactory> {
                on { getSlug(any()) } doReturn Slug.PretalxSlug("pretalx-slug")
            }
            val viewModel = createViewModel(repository, slugFactory = slugFactory)
            viewModel.onViewEvent(OnAppLinkReceived(mock()))
            viewModel.openSessionDetails.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
        }

        @Test
        fun `OnAppLinkReceived does not post to openSessionDetails property when slug is null`() = runTest {
            val repository = createRepository(updatedSelectedSessionId = true)
            val slugFactory = mock<SlugFactory> {
                on { getSlug(any()) } doReturn null
            }
            val viewModel = createViewModel(repository, slugFactory = slugFactory)
            viewModel.onViewEvent(OnAppLinkReceived(mock()))
            viewModel.openSessionDetails.test {
                expectNoEvents()
            }
        }

        @Test
        fun `OnAppLinkReceived does not post to openSessionDetails property when slug is not present`() = runTest {
            val repository = createRepository(updatedSelectedSessionId = false)
            val slugFactory = mock<SlugFactory> {
                on { getSlug(any()) } doReturn Slug.PretalxSlug("pretalx-slug")
            }
            val viewModel = createViewModel(repository, slugFactory = slugFactory)
            viewModel.onViewEvent(OnAppLinkReceived(mock()))
            viewModel.openSessionDetails.test {
                expectNoEvents()
            }
        }

        @Test
        fun `OnChangeStatisticsFavoritesFilterToggle persists the setting and recomputes changeStatisticsUiState property`() =
            runTest {
                val allChangedSessions = listOf(
                    SessionDatabaseModel(sessionId = "changed-01", changedIsNew = true),
                    SessionDatabaseModel(sessionId = "changed-02", changedIsCanceled = true, isFavorite = true),
                )
                val favoriteChangedSessions = allChangedSessions.filter { it.isFavorite }
                val repository = createRepository(
                    loadScheduleStateFlow = flowOf(ParseSuccess),
                    scheduleChangesSeen = false,
                    changedSessions = allChangedSessions,
                )
                // Mimics the setting being persisted and taken into account when loading.
                var favoritesFilterEnabled = false
                whenever(repository.readScheduleChangesFavoritesFilterEnabled()).thenAnswer { favoritesFilterEnabled }
                whenever(repository.loadFilteredChangedSessions()).thenAnswer {
                    if (favoritesFilterEnabled) favoriteChangedSessions else allChangedSessions
                }
                whenever(repository.updateScheduleChangesFavoritesFilterEnabled(any())).thenAnswer {
                    favoritesFilterEnabled = it.getArgument(0)
                    Unit
                }
                val changeStatisticsUiStateFactory = mock<ChangeStatisticsUiStateFactory> {
                    on { createChangeStatisticsUiState(any(), any(), any(), any()) } doAnswer { invocation ->
                        val changedSessions = invocation.getArgument<List<Session>>(0)
                        val properties = listOf(ChangeStatisticProperty(changedSessions.size, CHANGED))
                        ChangeStatisticsUiState("", properties, 10, favoritesFilterEnabled = invocation.getArgument(3))
                    }
                }
                val viewModel = createViewModel(repository, changeStatisticsUiStateFactory = changeStatisticsUiStateFactory)
                viewModel.changeStatisticsUiState.test {
                    val unfiltered = awaitItem()!!
                    assertThat(unfiltered.statistics.single().value).isEqualTo(2)
                    assertThat(unfiltered.favoritesFilterEnabled).isFalse()

                    viewModel.onViewEvent(OnChangeStatisticsFavoritesFilterToggle(enabled = true))
                    val filtered = awaitItem()!!
                    assertThat(filtered.statistics.single().value).isEqualTo(1)
                    assertThat(filtered.favoritesFilterEnabled).isTrue()
                }
                verifyInvokedOnce(repository).updateScheduleChangesFavoritesFilterEnabled(true)
            }

        @Test
        fun `OnCloseChangeStatisticsScreen post to openSessionChanges property`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnCloseChangeStatisticsScreen(shouldOpenSessionChanges = true))
            viewModel.openSessionChanges.test {
                assertThat(awaitItem()).isEqualTo(Unit)
                expectNoEvents()
            }
            verifyInvokedOnce(repository).updateScheduleChangesSeen(true)
        }

        @Test
        fun `OnCloseChangeStatisticsScreen does not post to openSessionChanges property`() = runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.onViewEvent(OnCloseChangeStatisticsScreen(shouldOpenSessionChanges = false))
            viewModel.openSessionChanges.test {
                expectNoEvents()
            }
            verifyInvokedOnce(repository).updateScheduleChangesSeen(true)
        }

        @Test
        fun `OnCheckPostNotificationsPermission does not post to missingPostNotificationsPermission property (no alarms)`() =
            runTest {
                val notificationHelper = mock<NotificationHelper> {
                    on { notificationsEnabled } doReturn true
                }
                val repository = createRepository(alarms = emptyList())
                val viewModel = createViewModel(repository, notificationHelper)
                viewModel.onViewEvent(OnCheckPostNotificationsPermission)
                viewModel.missingPostNotificationsPermission.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `OnCheckPostNotificationsPermission does not post to missingPostNotificationsPermission property (notifications enabled)`() =
            runTest {
                val notificationHelper = mock<NotificationHelper> {
                    on { notificationsEnabled } doReturn true
                }
                val repository = createRepository(alarms = listOf(mock()))
                val viewModel = createViewModel(repository, notificationHelper)
                viewModel.onViewEvent(OnCheckPostNotificationsPermission)
                viewModel.missingPostNotificationsPermission.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `OnCheckPostNotificationsPermission posts to missingPostNotificationsPermission property`() =
            runTest {
                val notificationHelper = mock<NotificationHelper> {
                    on { notificationsEnabled } doReturn false
                }
                val repository = createRepository(alarms = listOf(mock()))
                val viewModel = createViewModel(repository, notificationHelper)
                viewModel.onViewEvent(OnCheckPostNotificationsPermission)
                viewModel.missingPostNotificationsPermission.test {
                    assertThat(awaitItem()).isEqualTo(Unit)
                }
            }
    }

    private class TestParseResult(
        override val isSuccess: Boolean = false
    ) : ParseResult

    private fun createRepository(
        loadScheduleStateFlow: Flow<LoadScheduleState> = emptyFlow(),
        engelsystemUriParsingErrorStateFlow: Flow<Error?> = emptyFlow(),
        scheduleChangesSeen: Boolean = true,
        changedSessions: List<SessionDatabaseModel> = emptyList(),
        updatedSelectedSessionId: Boolean = false,
        alarms: List<Alarm> = emptyList(),
        filteredChangedSessions: List<SessionDatabaseModel> = changedSessions,
    ) = mock<AppRepository> {
        on { loadScheduleState } doReturn loadScheduleStateFlow
        on { engelsystemUriParsingErrorState } doReturn engelsystemUriParsingErrorStateFlow
        on { readScheduleChangesSeen() } doReturn scheduleChangesSeen
        on { readMeta() } doReturn Meta(version = "")
        on { loadFilteredChangedSessions() } doReturn filteredChangedSessions
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
            on { createChangeStatisticsUiState(any(), any(), any(), any()) } doReturn uiState
        }

    private fun createChangeStatisticsUiState(): ChangeStatisticsUiState {
        val properties = listOf(
            ChangeStatisticProperty(1, CHANGED),
            ChangeStatisticProperty(2, NEW),
            ChangeStatisticProperty(3, CANCELED),
        )
        return ChangeStatisticsUiState("", properties, 10, favoritesFilterEnabled = false)
    }


}
