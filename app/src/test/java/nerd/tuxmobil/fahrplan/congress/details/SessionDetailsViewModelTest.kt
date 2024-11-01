package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import androidx.core.net.toUri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Room
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class SessionDetailsViewModelTest {

    private companion object {
        val NO_TIME_ZONE_ID = null
        const val SAMPLE_FEEDBACK_URL = "http://conference.net/feedback"
        const val SAMPLE_SESSION_URL = "https://conference.net/program/famous-talk.html"
    }

    @Test
    fun `selectedSessionParameter does not emit SelectedSessionParameter`() = runTest {
        val repository = createRepository(selectedSessionFlow = emptyFlow())
        val fakePropertiesFormatter = mock<SessionPropertiesFormatter> {
            on { getFormattedLinks(any()) } doReturn "not relevant"
            on { getFormattedUrl(any()) } doReturn """<a href="$SAMPLE_SESSION_URL">$SAMPLE_SESSION_URL</a>"""
        }
        val fakeSessionUrlComposition = mock<SessionUrlComposition> {
            on { getSessionUrl(any()) } doReturn SAMPLE_SESSION_URL
        }
        val fakeFormattingDelegate = mock<FormattingDelegate> {
            on { getFormattedDateTimeShort(any(), any(), anyOrNull()) } doReturn "01.11.2021 13:00"
            on { getFormattedDateTimeLong(any(), any(), anyOrNull()) } doReturn "November 1, 2021 13:00"
        }
        val fakeMarkdownConversion = mock<MarkdownConversion> {
            on { markdownLinksToHtmlLinks(any()) } doReturn "Markdown"
        }
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn SAMPLE_FEEDBACK_URL
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionPropertiesFormatter = fakePropertiesFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        viewModel.selectedSessionParameter.test {
            awaitComplete()
        }
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `selectedSessionParameter emits SelectedSessionParameter built from filled sample session`() = runTest {
        val session = Session(
            sessionId = "S1",
            dateUTC = 100,
            title = "Session title",
            subtitle = "Session subtitle",
            speakers = listOf("Jane Doe", "John Doe"),
            roomName = "Main hall",
            roomIdentifier = "88888888-4444-4444-4444-121212121212",
            abstractt = "Session abstract",
            description = "Session description",
            track = "Session track",
            links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)",
            isHighlight = true,
        )
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val fakeSessionPropertiesFormatter = mock<SessionPropertiesFormatter> {
            on { getFormattedLinks(any()) } doReturn "not relevant"
            on { getFormattedUrl(any()) } doReturn """<a href="$SAMPLE_SESSION_URL">$SAMPLE_SESSION_URL</a>"""
            on { getFormattedSpeakers(any()) } doReturn "Jane Doe, John Doe"
        }
        val fakeSessionUrlComposition = mock<SessionUrlComposition> {
            on { getSessionUrl(any()) } doReturn SAMPLE_SESSION_URL
        }
        val fakeFormattingDelegate = mock<FormattingDelegate> {
            on { getFormattedDateTimeShort(any(), any(), anyOrNull()) } doReturn "01.11.2021 13:00"
            on { getFormattedDateTimeLong(any(), any(), anyOrNull()) } doReturn "November 1, 2021 13:00"
        }
        val fakeMarkdownConversion = mock<MarkdownConversion> {
            on { markdownLinksToHtmlLinks(any()) } doReturn "Markdown"
        }
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn SAMPLE_FEEDBACK_URL
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionPropertiesFormatter = fakeSessionPropertiesFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            indoorNavigation = SupportedIndoorNavigation,
        )
        val selectedSessionParameter = SelectedSessionParameter(
            hasDateUtc = true,
            formattedZonedDateTimeShort = "01.11.2021 13:00",
            formattedZonedDateTimeLong = "November 1, 2021 13:00",
            roomName = "Main hall",
            sessionId = "S1",
            title = "Session title",
            subtitle = "Session subtitle",
            speakerNames = "Jane Doe, John Doe",
            speakersCount = 2,
            formattedAbstract = "Markdown",
            abstract = "Session abstract",
            formattedDescription = "Markdown",
            description = "Session description",
            track = "Session track",
            hasLinks = true,
            formattedLinks = "Markdown",
            hasWikiLinks = false,
            sessionLink = """<a href="$SAMPLE_SESSION_URL">$SAMPLE_SESSION_URL</a>""",
            isFlaggedAsFavorite = true,
            hasAlarm = false,
            supportsFeedback = true,
            supportsIndoorNavigation = true,
        )
        viewModel.selectedSessionParameter.test {
            assertThat(awaitItem()).isEqualTo(selectedSessionParameter)
            awaitComplete()
        }
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `selectedSessionParameter emits SelectedSessionParameter built from empty sample session`() = runTest {
        val session = Session(
            sessionId = "S1",
            dateUTC = 0,
            title = "",
            subtitle = "",
            speakers = emptyList(),
            roomName = "",
            roomIdentifier = "",
            abstractt = "",
            description = "",
            track = "",
            links = "",
            isHighlight = false,
        )
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val fakeSessionPropertiesFormatter = mock<SessionPropertiesFormatter> {
            on { getFormattedLinks(any()) } doReturn "not relevant"
            on { getFormattedUrl(any()) } doReturn ""
            on { getFormattedSpeakers(any()) } doReturn ""
        }
        val fakeSessionUrlComposition = mock<SessionUrlComposition> {
            on { getSessionUrl(any()) } doReturn ""
        }
        val fakeFormattingDelegate = mock<FormattingDelegate> {
            on { getFormattedDateTimeShort(any(), any(), anyOrNull()) } doReturn ""
            on { getFormattedDateTimeLong(any(), any(), anyOrNull()) } doReturn ""
        }
        val fakeMarkdownConversion = mock<MarkdownConversion> {
            on { markdownLinksToHtmlLinks(any()) } doReturn ""
        }
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn ""
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionPropertiesFormatter = fakeSessionPropertiesFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            indoorNavigation = UnsupportedIndoorNavigation,
        )
        val selectedSessionParameter = SelectedSessionParameter(
            hasDateUtc = false,
            formattedZonedDateTimeShort = "",
            formattedZonedDateTimeLong = "",
            roomName = "",
            sessionId = "S1",
            title = "",
            subtitle = "",
            speakerNames = "",
            speakersCount = 0,
            formattedAbstract = "",
            abstract = "",
            formattedDescription = "",
            description = "",
            track = "",
            hasLinks = false,
            formattedLinks = "",
            hasWikiLinks = false,
            sessionLink = "",
            isFlaggedAsFavorite = false,
            hasAlarm = false,
            supportsFeedback = false,
            supportsIndoorNavigation = false,
        )
        viewModel.selectedSessionParameter.test {
            assertThat(awaitItem()).isEqualTo(selectedSessionParameter)
            awaitComplete()
        }
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `openFeedback() posts to openFeedback`() = runTest {
        val repository = createRepository()
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn SAMPLE_FEEDBACK_URL
        }
        val viewModel = createViewModel(repository, feedbackUrlComposer = fakeFeedbackUrlComposer)
        viewModel.openFeedback()
        viewModel.openFeedBack.test {
            assertThat(awaitItem()).isEqualTo(SAMPLE_FEEDBACK_URL.toUri())
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `share() posts to shareSimple what simpleSessionFormat returns`() = runTest {
        val repository = createRepository()
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any(), anyOrNull(), any()) } doReturn "An example session"
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share()
        viewModel.shareSimple.test {
            assertThat(awaitItem()).isEqualTo("An example session")
        }
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).readMeta()
    }

    @Test
    fun `shareToChaosflix() posts to shareJson what jsonSessionFormat returns`() = runTest {
        val repository = createRepository()
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<Session>()) } doReturn """{ "session" : "example" }"""
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix()
        viewModel.shareJson.test {
            assertThat(awaitItem()).isEqualTo("""{ "session" : "example" }""")
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `addToCalendar() posts to addToCalendar`() = runTest {
        val repository = createRepository(selectedSession = Session("S2"))
        val viewModel = createViewModel(repository)
        viewModel.addToCalendar()
        verifyInvokedOnce(repository).loadSelectedSession()
        viewModel.addToCalendar.test {
            assertThat(awaitItem()).isEqualTo(Session("S2"))
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
    fun `addAlarmWithChecks() posts to showAlarmTimePicker`() = runTest {
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
        viewModel.showAlarmTimePicker.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() posts to requestScheduleExactAlarmsPermission`() = runTest {
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
        viewModel.requestScheduleExactAlarmsPermission.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() posts to requestPostNotificationsPermission as of Android 13`() = runTest {
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
        viewModel.requestPostNotificationsPermission.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
        verifyInvokedOnce(notificationHelper).notificationsEnabled
    }

    @Test
    fun `addAlarmWithChecks() posts to notificationsDisabled before Android 13`() = runTest {
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
        viewModel.notificationsDisabled.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `addAlarm() persists the alarm deletion`() {
        val repository = createRepository(selectedSession = Session("S5"))
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository, alarmServices)
        viewModel.addAlarm(alarmTimesIndex = 1)
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
        val viewModel = createViewModel(repository, alarmServices)
        viewModel.deleteAlarm()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(alarmServices).deleteSessionAlarm(any())
    }

    @Test
    fun `closeDetails() posts to closeDetails`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.closeDetails()
        viewModel.closeDetails.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `navigateToRoom() posts to navigateToRoom`() = runTest {
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
        viewModel.navigateToRoom.test {
            assertThat(awaitItem()).isEqualTo("https://c3nav.foo/garden".toUri())
        }
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `supportsFeedback returns false if room name matches default Engelsystem room name`() = runTest {
        val session = Session(
            sessionId = "S1",
            roomName = "Engelshifts",
            roomIdentifier = "88888888-4444-4444-4444-121212121212",
        )
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val fakeSessionPropertiesFormatter = mock<SessionPropertiesFormatter> {
            on { getFormattedLinks(any()) } doReturn "not relevant"
            on { getFormattedUrl(any()) } doReturn ""
            on { getFormattedSpeakers(any()) } doReturn "not relevant"
        }
        val fakeSessionUrlComposition = mock<SessionUrlComposition> {
            on { getSessionUrl(any()) } doReturn ""
        }
        val fakeFormattingDelegate = mock<FormattingDelegate> {
            on { getFormattedDateTimeShort(any(), any(), anyOrNull()) } doReturn ""
            on { getFormattedDateTimeLong(any(), any(), anyOrNull()) } doReturn ""
        }
        val fakeMarkdownConversion = mock<MarkdownConversion> {
            on { markdownLinksToHtmlLinks(any()) } doReturn ""
        }
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn ""
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionPropertiesFormatter = fakeSessionPropertiesFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            indoorNavigation = UnsupportedIndoorNavigation,
            defaultEngelsystemRoomName = "Engelshifts",
            customEngelsystemRoomName = "Zengelshifts",
        )
        viewModel.selectedSessionParameter.test {
            val actualSessionParameter = awaitItem()
            assertThat(actualSessionParameter.roomName).isEqualTo("Zengelshifts")
            assertThat(actualSessionParameter.supportsFeedback).isFalse()
            awaitComplete()
        }
    }

    private fun createRepository(
        selectedSessionFlow: Flow<Session> = emptyFlow(),
        selectedSession: Session = Session("S0"),
        meta: Meta = Meta(numDays = 0, timeZoneId = NO_TIME_ZONE_ID),
        alarms: List<Alarm> = emptyList()
    ) = mock<AppRepository> {
        on { this.selectedSession } doReturn selectedSessionFlow
        on { loadSelectedSession() } doReturn selectedSession
        on { readMeta() } doReturn meta
        on { readAlarms(any()) } doReturn alarms
    }

    private fun createViewModel(
        repository: AppRepository,
        alarmServices: AlarmServices = mock(),
        notificationHelper: NotificationHelper = mock(),
        sessionPropertiesFormatter: SessionPropertiesFormatter = SessionPropertiesFormatter(),
        simpleSessionFormat: SimpleSessionFormat = mock(),
        jsonSessionFormat: JsonSessionFormat = mock(),
        feedbackUrlComposer: FeedbackUrlComposer = mock(),
        sessionUrlComposition: SessionUrlComposition = mock(),
        markdownConversion: MarkdownConversion = mock(),
        formattingDelegate: FormattingDelegate = mock(),
        indoorNavigation: IndoorNavigation = mock(),
        defaultEngelsystemRoomName: String = "Engelshifts",
        customEngelsystemRoomName: String = "Trollshifts",
        runsAtLeastOnAndroidTiramisu: Boolean = false
    ) = SessionDetailsViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        alarmServices = alarmServices,
        notificationHelper = notificationHelper,
        sessionPropertiesFormatter = sessionPropertiesFormatter,
        simpleSessionFormat = simpleSessionFormat,
        jsonSessionFormat = jsonSessionFormat,
        feedbackUrlComposer = feedbackUrlComposer,
        sessionUrlComposition = sessionUrlComposition,
        indoorNavigation = indoorNavigation,
        markdownConversion = markdownConversion,
        formattingDelegate = formattingDelegate,
        defaultEngelsystemRoomName = defaultEngelsystemRoomName,
        customEngelsystemRoomName = customEngelsystemRoomName,
        runsAtLeastOnAndroidTiramisu = runsAtLeastOnAndroidTiramisu
    )

    private object SupportedIndoorNavigation : IndoorNavigation {
        override fun isSupported(room: Room) = true
        override fun getUri(room: Room) = "https://c3nav.foo/garden".toUri()
    }

    private object UnsupportedIndoorNavigation : IndoorNavigation {
        override fun isSupported(room: Room) = false
        override fun getUri(room: Room): Uri = Uri.EMPTY
    }

}
