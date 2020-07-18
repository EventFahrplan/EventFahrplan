package nerd.tuxmobil.fahrplan.congress.details

import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.Test

class SessionDetailsViewModelTest {

    private val repository = mock<AppRepository>()
    private val actualSession = Session("S1")
    private val expectedSession = Session("S1")
    private val viewActionHandler = mock<SessionDetailsViewModel.ViewActionHandler>()
    private val defaultViewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler)

    companion object {
        private const val UNKNOWN_MENU_ITEM_ID = Int.MIN_VALUE
        private const val SAMPLE_URL = "http://example.com"
    }

    @Test
    fun `onOptionsMenuItemSelected returns false for unknown menu item id`() {
        assertThat(defaultViewModel.onOptionsMenuItemSelected(UNKNOWN_MENU_ITEM_ID)).isFalse()
    }

    @Test
    fun `onOptionsMenuItemSelected invokes openFeedback with URI`() {
        val toFeedbackUrl: Session.(String) -> String = { SAMPLE_URL }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler,
                toFeedbackUrl = toFeedbackUrl)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_feedback)).isTrue()
        verifyInvokedOnce(viewActionHandler).openFeedback(SAMPLE_URL.toUri())
    }

    @Test
    fun `onOptionsMenuItemSelected invokes shareAsPlainText with plain text`() {
        val toPlainText: Session.() -> String = { "An example session" }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler,
                toPlainText = toPlainText)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_share_session)).isTrue()
        verifyInvokedOnce(viewActionHandler).shareAsPlainText("An example session")
    }

    @Test
    fun `onOptionsMenuItemSelected invokes shareAsJson with JSON`() {
        val toJson: Session.() -> String = { """{ "session" : "example" }""" }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler,
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
        val actualSession = Session("S2").apply { highlight = false }
        val expectedSession = Session("S2").apply { highlight = true }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_flag_as_favorite)).isTrue()
        // TODO Simplify by comparing objects as soon as "highlight" is part of Session#equals.
        assertThat(actualSession.highlight).isEqualTo(expectedSession.highlight)
        verifyInvokedOnce(repository).updateHighlight(expectedSession)
        verifyInvokedOnce(repository).notifyHighlightsChanged()
        verifyInvokedOnce(viewActionHandler).refreshUI()
    }

    @Test
    fun `onOptionsMenuItemSelected unflags highlight and invokes updateHighlight, notifyHighlightsChanged, refreshUI`() {
        val actualSession = Session("S3").apply { highlight = true }
        val expectedSession = Session("S3").apply { highlight = false }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler)
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
        val actualSession = Session("S4").apply { room = "GARDEN" }
        val toC3NavRoomName: Session.() -> String = { this.room.toLowerCase() }
        val viewModel = SessionDetailsViewModel(repository, actualSession, viewActionHandler,
                c3NavBaseUrl = "https://c3nav.foo/", toC3NavRoomName = toC3NavRoomName)
        assertThat(viewModel.onOptionsMenuItemSelected(R.id.menu_item_navigate)).isTrue()
        verifyInvokedOnce(viewActionHandler).navigateToRoom("https://c3nav.foo/garden".toUri())
    }

}
