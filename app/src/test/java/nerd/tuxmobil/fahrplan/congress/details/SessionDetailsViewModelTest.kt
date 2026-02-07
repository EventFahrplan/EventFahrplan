package nerd.tuxmobil.fahrplan.congress.details

import androidx.core.net.toUri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.kotlin.library.roomstates.base.models.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.CloseDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.NavigateToRoom
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.OpenFeedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestPostNotificationsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestScheduleExactAlarmsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowAlarmTimePicker
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowNotificationsDisabledError
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsParameter.SessionDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Loading
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Success
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnSessionLinkClick
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Room
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.roomstates.RoomStateFormatting
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import info.metadude.kotlin.library.roomstates.base.models.Room as FosdemRoom

@ExtendWith(MainDispatcherTestExtension::class)
class SessionDetailsViewModelTest {

    private companion object {
        val NO_TIME_ZONE_ID = null
        const val SAMPLE_FEEDBACK_URL = "http://conference.net/feedback"
    }

    @Test
    fun `sessionDetailsState does emit Loading`() = runTest {
        val repository = createRepository(selectedSessionFlow = emptyFlow())
        val viewModel = createViewModel(
            repository = repository,
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.sessionDetailsState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
            expectNoEvents()
        }
    }

    @Test
    fun `sessionDetailsState does emit Success`() = runTest {
        val session = Session(sessionId = "S1")
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val viewModel = createViewModel(
            repository = repository,
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.sessionDetailsState.test {
            assertThat(awaitItem()).isInstanceOf(Success::class.java)
            expectNoEvents()
        }
    }

    @Test
    fun `selectedSessionParameter does not emit SelectedSessionParameter`() = runTest {
        val repository = createRepository(selectedSessionFlow = emptyFlow())
        val viewModel = createViewModel(
            repository = repository,
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.selectedSessionParameter.test {
            awaitComplete()
        }
    }

    @Test
    fun `selectedSessionParameter emits SelectedSessionParameter built from some session`() = runTest {
        val session = Session(sessionId = "S1")
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val viewModel = createViewModel(
            repository = repository,
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.selectedSessionParameter.test {
            assertThat(awaitItem()).isInstanceOf(SelectedSessionParameter::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `openFeedback() emits OpenFeedback effect`() = runTest {
        val repository = createRepository()
        val fakeFeedbackUrlComposition = mock<FeedbackUrlComposition> {
            on { getFeedbackUrl(any()) } doReturn SAMPLE_FEEDBACK_URL
        }
        val viewModel = createViewModel(repository, feedbackUrlComposition = fakeFeedbackUrlComposition)
        viewModel.openFeedback()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(OpenFeedback::class.java)
            assertThat((effect as OpenFeedback).uri).isEqualTo(SAMPLE_FEEDBACK_URL.toUri())
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `share() emits ShareSimple effect with formatted session`() = runTest {
        val repository = createRepository()
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any(), anyOrNull(), any()) } doReturn "An example session"
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ShareSimple::class.java)
            assertThat((effect as ShareSimple).formattedSession).isEqualTo("An example session")
        }
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).readMeta()
    }

    @Test
    fun `shareToChaosflix() emits ShareJson effect with formatted session`() = runTest {
        val repository = createRepository()
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<Session>()) } doReturn """{ "session" : "example" }"""
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ShareJson::class.java)
            assertThat((effect as ShareJson).formattedSession).isEqualTo("""{ "session" : "example" }""")
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `addToCalendar() emits AddToCalendar effect`() = runTest {
        val repository = createRepository(selectedSession = Session("S2"))
        val viewModel = createViewModel(repository)
        viewModel.addToCalendar()
        verifyInvokedOnce(repository).loadSelectedSession()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddToCalendar::class.java)
            assertThat((effect as AddToCalendar).session).isEqualTo(Session("S2"))
        }
    }

    @Test
    fun `favorSession() flags the session as a favorite and persists it`() {
        val actualSession = Session(sessionId = "S3", isHighlight = false)
        val expectedSession = Session(sessionId = "S3", isHighlight = true)
        val repository = createRepository(selectedSession = actualSession)
        val viewModel = createViewModel(repository)
        viewModel.favorSession()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
    }

    @Test
    fun `unfavorSession() unflags the session as a favorite and persists it`() {
        val actualSession = Session(sessionId = "S4", isHighlight = true)
        val expectedSession = Session(sessionId = "S4", isHighlight = false)
        val repository = createRepository(selectedSession = actualSession)
        val viewModel = createViewModel(repository)
        viewModel.unfavorSession()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
    }

    @Test
    fun `canAddAlarms invokes canScheduleExactAlarms property`() {
        val repository = createRepository()
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository = repository, alarmServices = alarmServices)
        viewModel.canAddAlarms()
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() emits ShowAlarmTimePicker effect`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val alarmServices = mock<AlarmServices> {
            on { canScheduleExactAlarms } doReturn true
        }
        val repository = createRepository()
        val viewModel = createViewModel(
            repository = repository,
            notificationHelper = notificationHelper,
            alarmServices = alarmServices,
        )
        viewModel.addAlarmWithChecks()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(ShowAlarmTimePicker)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() emits RequestScheduleExactAlarmsPermission effect`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val alarmServices = mock<AlarmServices> {
            on { canScheduleExactAlarms } doReturn false
        }
        val repository = createRepository()
        val viewModel = createViewModel(
            repository = repository,
            notificationHelper = notificationHelper,
            alarmServices = alarmServices,
            runsAtLeastOnAndroidTiramisu = true, // not relevant
        )
        viewModel.addAlarmWithChecks()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(RequestScheduleExactAlarmsPermission)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() emits RequestPostNotificationsPermission effect as of Android 13`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn false
        }
        val repository = createRepository()
        val viewModel = createViewModel(
            repository = repository,
            notificationHelper = notificationHelper,
            runsAtLeastOnAndroidTiramisu = true,
        )
        viewModel.addAlarmWithChecks()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(RequestPostNotificationsPermission)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
    }

    @Test
    fun `addAlarmWithChecks() emits ShowNotificationsDisabledError effect before Android 13`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn false
        }
        val repository = createRepository()
        val viewModel = createViewModel(
            repository = repository,
            notificationHelper = notificationHelper,
            runsAtLeastOnAndroidTiramisu = false,
        )
        viewModel.addAlarmWithChecks()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(ShowNotificationsDisabledError)
        }
    }

    @Test
    fun `addAlarm() persists the alarm deletion`() {
        val repository = createRepository(selectedSession = Session("S5"))
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository, alarmServices = alarmServices)
        viewModel.addAlarm(alarmTime = 5)
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(alarmServices).addSessionAlarm(any(), any())
    }

    @Test
    fun `deleteAlarm() persists the alarm deletion`() {
        val repository = createRepository(
            selectedSession = Session("S6"),
            alarms = emptyList()
        )
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository, alarmServices = alarmServices)
        viewModel.deleteAlarm()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(alarmServices).deleteSessionAlarm(any())
    }

    @Test
    fun `closeDetails() emits CloseDetails effect`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.closeDetails()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(CloseDetails)
        }
    }

    @Test
    fun `navigateToRoom() emits NavigateToRoom effect`() = runTest {
        val repository = createRepository(
            selectedSession = Session(
                sessionId = "S1",
                roomName = "Garden",
                roomIdentifier = "",
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.navigateToRoom()
        viewModel.effects.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(NavigateToRoom::class.java)
            assertThat((effect as NavigateToRoom).uri).isEqualTo("https://c3nav.foo/garden".toUri())
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `OnSessionLinkClick invokes openLink if no default browseable app nor browser apps are present`() = runTest {
        val link = "https://events.ccc.de/congress/2025/hub/event/detail/opening-ceremony"
        var invokedLink = ""
        val externalNavigation = object : ExternalNavigation {
            override fun openMap(locationText: String) = throw NotImplementedError()
            override fun getBrowserApps() = emptyList<String>()
            override fun getDefaultBrowsableApp() = null
            override fun openLink(link: String) {
                invokedLink = link
            }

            override fun openLinkWithApp(link: String, packageName: String) =
                throw NotImplementedError()
        }
        val viewModel = createViewModel(
            externalNavigation = externalNavigation,
        )
        viewModel.onViewEvent(OnSessionLinkClick(link))
        assertThat(invokedLink).isEqualTo(link)
    }

    @Test
    fun `OnSessionLinkClick invokes openLink if the only browser apps is the app itself`() =
        runTest {
            val link = "https://events.ccc.de/congress/2025/hub/event/detail/opening-ceremony"
            var invokedLink = ""
            val buildConfigProvision = mock<BuildConfigProvision> {
                on { packageName } doReturn "com.example.app"
            }
            val externalNavigation = object : ExternalNavigation {
                override fun openMap(locationText: String) = throw NotImplementedError()
                override fun getDefaultBrowsableApp() = null
                override fun getBrowserApps() = listOf("com.example.app")

                override fun openLink(link: String) {
                    invokedLink = link
                }

                override fun openLinkWithApp(link: String, packageName: String) =
                    throw NotImplementedError()
            }
            val viewModel = createViewModel(
                buildConfigProvision = buildConfigProvision,
                externalNavigation = externalNavigation,
            )
            viewModel.onViewEvent(OnSessionLinkClick(link))
            assertThat(invokedLink).isEqualTo(link)
        }

    @Test
    fun `OnSessionLinkClick invokes openLinkWithApp if at least one other browser app is present`() =
        runTest {
            val link = "https://events.ccc.de/congress/2025/hub/event/detail/opening-ceremony"
            var invokedLink = ""
            var invokedPackage = ""
            val buildConfigProvision = mock<BuildConfigProvision> {
                on { packageName } doReturn "com.example.app"
            }
            val externalNavigation = object : ExternalNavigation {
                override fun openMap(locationText: String) = throw NotImplementedError()
                override fun getDefaultBrowsableApp() = null
                override fun getBrowserApps() = listOf("com.example.browser")
                override fun openLink(link: String) = throw NotImplementedError()
                override fun openLinkWithApp(link: String, packageName: String) {
                    invokedLink = link
                    invokedPackage = packageName
                }
            }
            val viewModel = createViewModel(
                buildConfigProvision = buildConfigProvision,
                externalNavigation = externalNavigation,
            )
            viewModel.onViewEvent(OnSessionLinkClick(link))
            assertThat(invokedLink).isEqualTo(link)
            assertThat(invokedPackage).isEqualTo("com.example.browser")
        }
    @Test
    fun `OnSessionLinkClick invokes openLinkWithApp if a default browseable app is present`() =
        runTest {
            val link = "https://events.ccc.de/congress/2025/hub/event/detail/opening-ceremony"
            var invokedLink = ""
            var invokedPackage = ""
            val buildConfigProvision = mock<BuildConfigProvision> {
                on { packageName } doReturn "com.example.app"
            }
            val externalNavigation = object : ExternalNavigation {
                override fun openMap(locationText: String) = throw NotImplementedError()
                override fun getDefaultBrowsableApp() = "com.example.browser2"
                override fun getBrowserApps() = listOf("com.example.browser1", "com.example.browser2")
                override fun openLink(link: String) = throw NotImplementedError()
                override fun openLinkWithApp(link: String, packageName: String) {
                    invokedLink = link
                    invokedPackage = packageName
                }
            }
            val viewModel = createViewModel(
                buildConfigProvision = buildConfigProvision,
                externalNavigation = externalNavigation,
            )
            viewModel.onViewEvent(OnSessionLinkClick(link))
            assertThat(invokedLink).isEqualTo(link)
            assertThat(invokedPackage).isEqualTo("com.example.browser2")
        }

    @Test
    fun `roomStateMessage emits unknown when feature is disabled`() = runTest {
        val repository = createRepository(
            selectedSessionFlow = emptyFlow(),
            roomStatesFlow = emptyFlow()
        )
        val logging = mock<Logging>()
        val viewModel = createViewModel(
            repository = repository,
            logging = logging,
            buildConfigProvision = mock<BuildConfigProvision>(), // disables room states feature
            roomStateFormatting = UnknownRoomStateFormatter
        )
        viewModel.roomStateMessage.test {
            assertThat(awaitItem()).isEqualTo("Unknown")
        }
        verify(repository, times(2)).selectedSession // once for sessionDetailsState
        verifyInvokedNever(repository).roomStates
        verifyInvokedNever(logging).e(any(), any())
    }

    @Test
    fun `roomStateMessage emits unknown state message per default`() = runTest {
        val repository = createRepository(
            selectedSessionFlow = emptyFlow(),
            roomStatesFlow = emptyFlow()
        )
        val logging = mock<Logging>()
        val viewModel = createViewModel(
            repository = repository,
            logging = logging,
            buildConfigProvision = EnableFosdemRoomStatesBuildConfig,
            roomStateFormatting = UnknownRoomStateFormatter
        )
        viewModel.roomStateMessage.test {
            assertThat(awaitItem()).isEqualTo("Unknown")
        }
        verify(repository, times(3)).selectedSession // once for sessionDetailsState
        verifyInvokedOnce(repository).roomStates
        verifyInvokedNever(logging).e(any(), any())
    }

    @Test
    fun `roomStateMessage emits room state message when room names match`() = runTest {
        val session = Session(
            sessionId = "S1",
            roomName = "Main hall",
        )
        val roomState = State.TOO_FULL
        val repository = createRepository(
            selectedSessionFlow = flowOf(session),
            roomStatesFlow = flowOf(Result.success(listOf(FosdemRoom("Main hall", roomState))))
        )
        val logging = mock<Logging>()
        val viewModel = createViewModel(
            repository = repository,
            logging = logging,
            buildConfigProvision = EnableFosdemRoomStatesBuildConfig,
            roomStateFormatting = CrowdedRoomStateFormatter
        )
        viewModel.roomStateMessage.test {
            assertThat(awaitItem()).isEqualTo("Crowded")
        }
        verify(repository, times(3)).selectedSession // once for sessionDetailsState
        verifyInvokedOnce(repository).roomStates
        verifyInvokedNever(logging).e(any(), any())
    }

    @Test
    fun `roomStateMessage emits unknown state message when room names do not match`() = runTest {
        val session = Session(
            sessionId = "S1",
            roomName = "Unknown room",
        )
        val roomState = State.TOO_FULL
        val repository = createRepository(
            selectedSessionFlow = flowOf(session),
            roomStatesFlow = flowOf(Result.success(listOf(FosdemRoom("Main hall", roomState))))
        )
        val logging = mock<Logging>()
        val viewModel = createViewModel(
            repository = repository,
            logging = logging,
            buildConfigProvision = EnableFosdemRoomStatesBuildConfig,
            roomStateFormatting = UnknownRoomStateFormatter
        )
        viewModel.roomStateMessage.test {
            assertThat(awaitItem()).isEqualTo("Unknown")
        }
        verify(repository, times(3)).selectedSession // once for sessionDetailsState
        verifyInvokedOnce(repository).roomStates
        verifyInvokedOnce(logging).e(any(), any())
    }

    @Test
    fun `roomStateMessage emits failure state message when room states cannot be fetched`() = runTest {
        val session = Session(
            sessionId = "S1",
            roomName = "Unknown room",
        )
        val repository = createRepository(
            selectedSessionFlow = flowOf(session),
            roomStatesFlow = flowOf(Result.failure(RuntimeException()))
        )
        val logging = mock<Logging>()
        val viewModel = createViewModel(
            repository = repository,
            logging = logging,
            buildConfigProvision = EnableFosdemRoomStatesBuildConfig,
            roomStateFormatting = UnknownRoomStateFormatter
        )
        viewModel.roomStateMessage.test {
            assertThat(awaitItem()).isEqualTo("Failure")
        }
        verify(repository, times(3)).selectedSession // once for sessionDetailsState
        verifyInvokedOnce(repository).roomStates
        verifyInvokedOnce(logging).e(any(), any())
    }

    private fun createRepository(
        selectedSessionFlow: Flow<Session> = emptyFlow(),
        roomStatesFlow: Flow<Result<List<FosdemRoom>>> = emptyFlow(),
        selectedSession: Session = Session("S0"),
        meta: Meta = Meta(numDays = 0, timeZoneId = NO_TIME_ZONE_ID),
        alarms: List<Alarm> = emptyList()
    ) = mock<AppRepository> {
        on { this.selectedSession } doReturn selectedSessionFlow
        on { this.roomStates } doReturn roomStatesFlow
        on { loadSelectedSession() } doReturn selectedSession
        on { readMeta() } doReturn meta
        on { readAlarms(any()) } doReturn alarms
    }

    private fun createViewModel(
        repository: AppRepository = createRepository(),
        logging: Logging = mock(),
        buildConfigProvision: BuildConfigProvision = mock(),
        alarmServices: AlarmServices = mock(),
        notificationHelper: NotificationHelper = mock(),
        externalNavigation: ExternalNavigation = mock(),
        simpleSessionFormat: SimpleSessionFormat = mock(),
        jsonSessionFormat: JsonSessionFormat = mock(),
        feedbackUrlComposition: FeedbackUrlComposition = mock(),
        roomStateFormatting: RoomStateFormatting = mock(),
        indoorNavigation: IndoorNavigation = mock(),
        runsAtLeastOnAndroidTiramisu: Boolean = false
    ) = SessionDetailsViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        logging = logging,
        buildConfigProvision = buildConfigProvision,
        alarmServices = alarmServices,
        notificationHelper = notificationHelper,
        externalNavigation = externalNavigation,
        sessionDetailsParameterFactory = createSessionDetailsParameterFactory(),
        selectedSessionParameterFactory = createSelectedSessionParameterFactory(),
        simpleSessionFormat = simpleSessionFormat,
        jsonSessionFormat = jsonSessionFormat,
        feedbackUrlComposition = feedbackUrlComposition,
        indoorNavigation = indoorNavigation,
        roomStateFormatting = roomStateFormatting,
        runsAtLeastOnAndroidTiramisu = runsAtLeastOnAndroidTiramisu
    )

    private fun createSessionDetailsParameterFactory() = mock<SessionDetailsParameterFactory> {
        on { createSessionDetailsParameters(any()) } doReturn SessionDetails(
            id = SessionDetailsProperty("", ""),
            title = SessionDetailsProperty("", ""),
            subtitle = SessionDetailsProperty("", ""),
            speakerNames = SessionDetailsProperty("", ""),
            abstract = SessionDetailsProperty(Markdown(""), ""),
            description = SessionDetailsProperty(Markdown(""), ""),
            trackName = SessionDetailsProperty("", ""),
            links = SessionDetailsProperty("", ""),
            startsAt = SessionDetailsProperty("", ""),
            roomName = SessionDetailsProperty("", ""),
            sessionLink = "",
        )
    }

    private fun createSelectedSessionParameterFactory() = mock<SelectedSessionParameterFactory> {
        on { createSelectedSessionParameter(any()) } doReturn SelectedSessionParameter(
            isFlaggedAsFavorite = false,
            hasAlarm = false,
            supportsFeedback = false,
            supportsIndoorNavigation = false,
        )
    }

    private object SupportedFeedbackUrlComposer : FeedbackUrlComposition {
        override fun getFeedbackUrl(session: Session) = SAMPLE_FEEDBACK_URL
    }

    private object SupportedIndoorNavigation : IndoorNavigation {
        override fun isSupported(room: Room) = true
        override fun getUri(room: Room) = "https://c3nav.foo/garden".toUri()
    }

    private object UnknownRoomStateFormatter : RoomStateFormatting {
        override fun getText(state: State?) = "Unknown"
        override fun getFailureText(throwable: Throwable) = "Failure"
    }

    private object CrowdedRoomStateFormatter : RoomStateFormatting {
        override fun getText(state: State?) = "Crowded"
        override fun getFailureText(throwable: Throwable) = "Failure"
    }

    private object EnableFosdemRoomStatesBuildConfig : BuildConfigProvision {
        override val packageName: String = ""
        override val versionName: String = ""
        override val versionCode: Int = 0
        override val eventPostalAddress: String = ""
        override val eventWebsiteUrl: String = ""
        override val showAppDisclaimer: Boolean = false
        override val translationPlatformUrl: String = ""
        override val sourceCodeUrl: String = ""
        override val issuesUrl: String = ""
        override val fDroidUrl: String = ""
        override val googlePlayUrl: String = ""
        override val dataPrivacyStatementDeUrl: String = ""
        override val enableFosdemRoomStates: Boolean = true
        override val fosdemRoomStatesPath: String = ""
        override val fosdemRoomStatesUrl: String = ""
        override val scheduleUrl: String = ""
        override val serverBackendType: String = ""
        override val enableEngelsystemShifts: Boolean = false

    }

}
