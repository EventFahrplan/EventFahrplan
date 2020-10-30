package nerd.tuxmobil.fahrplan.congress.details

import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.ZoneId

class SessionDetailsViewModelTest {

    private val repository = mock<AppRepository>()
    private val actualSession = Session(ACTUAL_SESSION_ID)
    private val expectedSession = Session(EXPECTED_SESSION_ID)
    private val viewActionHandler = mock<SessionDetailsViewModel.ViewActionHandler>()
    private val meta = mock<Meta>()
    private lateinit var defaultViewModel: SessionDetailsViewModel

    companion object {
        private val NO_TIME_ZONE_ID = null
        private const val UNKNOWN_MENU_ITEM_ID = Int.MIN_VALUE
        private const val SAMPLE_URL = "http://example.com"
        private const val ACTUAL_SESSION_ID = "11111111-1111-1111-1111-111111111111"
        private const val EXPECTED_SESSION_ID = "11111111-1111-1111-1111-111111111111"
    }

    @Before
    fun setUp() {
        whenever(repository.readSessionBySessionId(anyString())) doReturn actualSession
        whenever(repository.readMeta()) doReturn meta
        whenever(meta.timeZoneId) doReturn NO_TIME_ZONE_ID
        // ViewModel must be initialized after stubbing the repository.
        defaultViewModel = SessionDetailsViewModel(repository, ACTUAL_SESSION_ID, viewActionHandler)
    }

    @Test
    fun `onOptionsMenuItemSelected returns false for unknown menu item id`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(UNKNOWN_MENU_ITEM_ID)).isFalse()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes openFeedback with URI`() {
        val toFeedbackUrl: Session.(String) -> String = { SAMPLE_URL }
        val viewModel = SessionDetailsViewModel(repository, ACTUAL_SESSION_ID, viewActionHandler,
                toFeedbackUrl = toFeedbackUrl)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_feedback)).isTrue()
        verifyInvokedOnce(viewActionHandler).openFeedback(SAMPLE_URL.toUri())
    }

    @Test
    fun `onOptionsMenuItemSelected invokes shareAsPlainText with plain text`() {
        val toPlainText: Session.(ZoneId?) -> String = { "An example session" }
        val viewModel = SessionDetailsViewModel(repository, ACTUAL_SESSION_ID, viewActionHandler,
                toPlainText = toPlainText)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_share_session)).isTrue()
        verifyInvokedOnce(viewActionHandler).shareAsPlainText("An example session")
    }

    @Test
    fun `onOptionsMenuItemSelected invokes shareAsJson with JSON`() {
        val toJson: Session.() -> String = { """{ "session" : "example" }""" }
        val viewModel = SessionDetailsViewModel(repository, ACTUAL_SESSION_ID, viewActionHandler,
                toJson = toJson)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_share_session_json)).isTrue()
        verifyInvokedOnce(viewActionHandler).shareAsJson("""{ "session" : "example" }""")
    }

    @Test
    fun `onOptionsMenuItemSelected invokes addToCalendar with session`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(R.id.menu_item_add_to_calendar)).isTrue()
        verifyInvokedOnce(viewActionHandler).addToCalendar(expectedSession)
    }

    @Test
    fun `onOptionsMenuItemSelected flags highlight and invokes updateHighlight, notifyHighlightsChanged, refreshUI`() {
        val actualSession = Session("22222222-2222-2222-2222-222222222222").apply { highlight = false }
        val expectedSession = Session("22222222-2222-2222-2222-222222222222").apply { highlight = true }
        whenever(repository.readSessionBySessionId("22222222-2222-2222-2222-222222222222")) doReturn actualSession
        val viewModel = SessionDetailsViewModel(repository, "22222222-2222-2222-2222-222222222222", viewActionHandler)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_flag_as_favorite)).isTrue()
        // TODO Simplify by comparing objects as soon as "highlight" is part of Session#equals.
        assertThat(actualSession.highlight).isEqualTo(expectedSession.highlight)
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
        verifyInvokedOnce(repository).notifyHighlightsChanged()
        verifyInvokedOnce(viewActionHandler).refreshUI()
    }

    @Test
    fun `onOptionsMenuItemSelected unflags highlight and invokes updateHighlight, notifyHighlightsChanged, refreshUI`() {
        val actualSession = Session("33333333-3333-3333-3333-333333333333").apply { highlight = true }
        val expectedSession = Session("33333333-3333-3333-3333-333333333333").apply { highlight = false }
        whenever(repository.readSessionBySessionId("33333333-3333-3333-3333-333333333333")) doReturn actualSession
        val viewModel = SessionDetailsViewModel(repository, "33333333-3333-3333-3333-333333333333", viewActionHandler)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_unflag_as_favorite)).isTrue()
        // TODO Simplify by comparing objects as soon as "highlight" is part of Session#equals.
        assertThat(actualSession.highlight).isEqualTo(expectedSession.highlight)
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
        verifyInvokedOnce(repository).notifyHighlightsChanged()
        verifyInvokedOnce(viewActionHandler).refreshUI()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes showAlarmTimePicker`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(R.id.menu_item_set_alarm)).isTrue()
        verifyInvokedOnce(viewActionHandler).showAlarmTimePicker()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes deleteAlarm, refreshUI`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(R.id.menu_item_delete_alarm)).isTrue()
        verifyInvokedOnce(viewActionHandler).deleteAlarm(expectedSession)
        verifyInvokedOnce(viewActionHandler).refreshUI()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes closeDetails`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(R.id.menu_item_close_session_details)).isTrue()
        verifyInvokedOnce(viewActionHandler).closeDetails()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes navigateToRoom with URI`() {
        val actualSession = Session("44444444-4444-4444-4444-444444444444").apply { room = "GARDEN" }
        whenever(repository.readSessionBySessionId("44444444-4444-4444-4444-444444444444")) doReturn actualSession
        val toC3NavRoomName: Session.() -> String = { this.room.toLowerCase() }
        val viewModel = SessionDetailsViewModel(repository, "44444444-4444-4444-4444-444444444444", viewActionHandler,
                c3NavBaseUrl = "https://c3nav.foo/", toC3NavRoomName = toC3NavRoomName)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_navigate)).isTrue()
        verifyInvokedOnce(viewActionHandler).navigateToRoom("https://c3nav.foo/garden".toUri())
    }

    @Test
    fun `formattedLinks returns an empty string when no links are present`() {
        assertThat(defaultViewModel.formattedLinks).isEmpty()
    }

    @Test
    fun `formattedLinks returns HTML formatted links`() {
        val links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)"
        val session = Session("55555555-5555-5555-5555-555555555555").apply { this.links = links }
        whenever(repository.readSessionBySessionId("55555555-5555-5555-5555-555555555555")) doReturn session
        val viewModel = SessionDetailsViewModel(repository, "55555555-5555-5555-5555-555555555555", viewActionHandler)
        val expectedFormattedLinks = """<a href="https://www.voc.com/projects/">VOC projects</a><br><a href="https://poc.com/QXut1XBymAk">POC</a>"""
        assertThat(viewModel.formattedLinks).isEqualTo(expectedFormattedLinks)
    }

    @Test
    fun `sessionLink returns an empty string when no session URL is composed`() {
        val toSessionUrl: Session.() -> String = { "" }
        val viewModel = SessionDetailsViewModel(repository, "66666666-6666-6666-6666-666666666666", viewActionHandler, toSessionUrl = toSessionUrl)
        assertThat(viewModel.sessionLink).isEmpty()
    }

    @Test
    fun `sessionLink returns the HTML formatted session link`() {
        val toSessionUrl: Session.() -> String = { "https://conference.net/program/${this.sessionId}.html" }
        val session = Session("famous-talk")
        whenever(repository.readSessionBySessionId("77777777-7777-7777-7777-777777777777")) doReturn session
        val viewModel = SessionDetailsViewModel(repository, "77777777-7777-7777-7777-777777777777", viewActionHandler, toSessionUrl = toSessionUrl)
        val expectedSessionLink = """<a href="https://conference.net/program/famous-talk.html">https://conference.net/program/famous-talk.html</a>"""
        assertThat(viewModel.sessionLink).isEqualTo(expectedSessionLink)
    }

}
