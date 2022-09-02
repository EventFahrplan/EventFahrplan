package nerd.tuxmobil.fahrplan.congress.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toUri
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestRule
import info.metadude.android.eventfahrplan.commons.testing.assertLiveData
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewModel.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SessionDetailsViewModelTest {

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private companion object {
        val NO_TIME_ZONE_ID = null
        const val SAMPLE_FEEDBACK_URL = "http://conference.net/feedback"
        const val SAMPLE_SESSION_URL = "https://conference.net/program/famous-talk.html"
    }

    @Test
    fun `selectedSessionParameter does not emit SelectedSessionParameter`() {
        val repository = createRepository(selectedSessionFlow = emptyFlow())
        val fakeSessionFormatter = mock<SessionFormatter> {
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
        val fakeRoomForC3NavConverter = mock<RoomForC3NavConverter> {
            on { convert(any()) } doReturn "Main hall"
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionFormatter = fakeSessionFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            roomForC3NavConverter = fakeRoomForC3NavConverter
        )
        assertLiveData(viewModel.selectedSessionParameter).isNull()
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `selectedSessionParameter emits SelectedSessionParameter built from filled sample session`() {
        val session = Session("S1").apply {
            dateUTC = 100
            title = "Session title"
            subtitle = "Session subtitle"
            speakers = listOf("Jane Doe", "John Doe")
            room = "Main hall"
            abstractt = "Session abstract"
            description = "Session description"
            track = "Session track"
            links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)"
            highlight = true
        }
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val fakeSessionFormatter = mock<SessionFormatter> {
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
        val fakeRoomForC3NavConverter = mock<RoomForC3NavConverter> {
            on { convert(any()) } doReturn "Main hall"
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionFormatter = fakeSessionFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            roomForC3NavConverter = fakeRoomForC3NavConverter
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
            isFeedbackUrlEmpty = false,
            isC3NavRoomNameEmpty = false,
        )
        assertLiveData(viewModel.selectedSessionParameter).isEqualTo(selectedSessionParameter)
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `selectedSessionParameter emits SelectedSessionParameter built from empty sample session`() {
        val session = Session("S1").apply {
            dateUTC = 0
            title = ""
            subtitle = ""
            speakers = emptyList()
            room = ""
            abstractt = ""
            description = ""
            track = ""
            links = ""
            highlight = false
        }
        val repository = createRepository(selectedSessionFlow = flowOf(session))
        val fakeSessionFormatter = mock<SessionFormatter> {
            on { getFormattedLinks(any()) } doReturn "not relevant"
            on { getFormattedUrl(any()) } doReturn ""
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
        val fakeRoomForC3NavConverter = mock<RoomForC3NavConverter> {
            on { convert(any()) } doReturn ""
        }
        val viewModel = createViewModel(
            repository = repository,
            sessionFormatter = fakeSessionFormatter,
            sessionUrlComposition = fakeSessionUrlComposition,
            formattingDelegate = fakeFormattingDelegate,
            markdownConversion = fakeMarkdownConversion,
            feedbackUrlComposer = fakeFeedbackUrlComposer,
            roomForC3NavConverter = fakeRoomForC3NavConverter
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
            isFeedbackUrlEmpty = true,
            isC3NavRoomNameEmpty = true,
        )
        assertLiveData(viewModel.selectedSessionParameter).isEqualTo(selectedSessionParameter)
        verifyInvokedOnce(repository).selectedSession
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `openFeedback() posts to openFeedback`() {
        val repository = createRepository()
        val fakeFeedbackUrlComposer = mock<FeedbackUrlComposer> {
            on { getFeedbackUrl(any()) } doReturn SAMPLE_FEEDBACK_URL
        }
        val viewModel = createViewModel(repository, feedbackUrlComposer = fakeFeedbackUrlComposer)
        viewModel.openFeedback()
        assertLiveData(viewModel.openFeedBack).isEqualTo(SAMPLE_FEEDBACK_URL.toUri())
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `share() posts to shareSimple what simpleSessionFormat returns`() {
        val repository = createRepository()
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any(), anyOrNull(), any()) } doReturn "An example session"
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share()
        assertLiveData(viewModel.shareSimple).isEqualTo("An example session")
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).readMeta()
    }

    @Test
    fun `shareToChaosflix() posts to shareJson what jsonSessionFormat returns`() {
        val repository = createRepository()
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<Session>()) } doReturn """{ "session" : "example" }"""
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix()
        assertLiveData(viewModel.shareJson).isEqualTo("""{ "session" : "example" }""")
        verifyInvokedOnce(repository).loadSelectedSession()
    }

    @Test
    fun `addToCalendar() posts to addToCalendar`() {
        val repository = createRepository(selectedSession = Session("S2"))
        val viewModel = createViewModel(repository)
        viewModel.addToCalendar()
        verifyInvokedOnce(repository).loadSelectedSession()
        assertLiveData(viewModel.addToCalendar).isEqualTo(Session("S2"))
    }

    @Test
    fun `favorSession() flags the session as a favorite and persists it`() {
        val actualSession = Session("S3").apply { highlight = false }
        val expectedSession = Session("S3").apply { highlight = true }
        val repository = createRepository(selectedSession = actualSession)
        val viewModel = createViewModel(repository)
        viewModel.favorSession()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
    }

    @Test
    fun `unfavorSession() unflags the session as a favorite and persists it`() {
        val actualSession = Session("S4").apply { highlight = true }
        val expectedSession = Session("S4").apply { highlight = false }
        val repository = createRepository(selectedSession = actualSession)
        val viewModel = createViewModel(repository)
        viewModel.unfavorSession()
        verifyInvokedOnce(repository).loadSelectedSession()
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
    }

    @Test
    fun `setAlarm() posts to setAlarm`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.setAlarm()
        assertLiveData(viewModel.setAlarm).isEqualTo(Unit)
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
    fun `closeDetails() posts to closeDetails`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.closeDetails()
        assertLiveData(viewModel.closeDetails).isEqualTo(Unit)
    }

    @Test
    fun `navigateToRoom() posts to navigateToRoom`() {
        val repository = createRepository()
        val fakeRoomForC3NavConverter = mock<RoomForC3NavConverter> {
            on { convert(anyOrNull()) } doReturn "garden"
        }
        val viewModel = createViewModel(
            repository = repository,
            roomForC3NavConverter = fakeRoomForC3NavConverter,
            c3NavBaseUrl = "https://c3nav.foo/"
        )
        viewModel.navigateToRoom()
        assertLiveData(viewModel.navigateToRoom).isEqualTo("https://c3nav.foo/garden".toUri())
        verifyInvokedOnce(repository).loadSelectedSession()
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
        sessionFormatter: SessionFormatter = mock(),
        simpleSessionFormat: SimpleSessionFormat = mock(),
        jsonSessionFormat: JsonSessionFormat = mock(),
        feedbackUrlComposer: FeedbackUrlComposer = mock(),
        sessionUrlComposition: SessionUrlComposition = mock(),
        roomForC3NavConverter: RoomForC3NavConverter = mock(),
        markdownConversion: MarkdownConversion = mock(),
        formattingDelegate: FormattingDelegate = mock(),
        c3NavBaseUrl: String = ""
    ) = SessionDetailsViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        alarmServices = alarmServices,
        sessionFormatter = sessionFormatter,
        simpleSessionFormat = simpleSessionFormat,
        jsonSessionFormat = jsonSessionFormat,
        feedbackUrlComposer = feedbackUrlComposer,
        sessionUrlComposition = sessionUrlComposition,
        roomForC3NavConverter = roomForC3NavConverter,
        markdownConversion = markdownConversion,
        formattingDelegate = formattingDelegate,
        c3NavBaseUrl = c3NavBaseUrl,
        defaultEngelsystemRoomName = "Engelshifts",
        customEngelsystemRoomName = "Trollshifts"
    )

}
