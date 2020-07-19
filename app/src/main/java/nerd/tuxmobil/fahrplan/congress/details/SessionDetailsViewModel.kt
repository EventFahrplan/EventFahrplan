package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import androidx.core.net.toUri
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.Font

class SessionDetailsViewModel @JvmOverloads constructor(

        private val repository: AppRepository,
        // TODO Pass sessionId and load Session from AppRepository as soon as Fragment depends on ViewModel
        private val session: Session,
        private val viewActionHandler: ViewActionHandler,

        // Session conversion parameters
        private val sessionFeedbackUrlTemplate: String = BuildConfig.SCHEDULE_FEEDBACK_URL,
        private val c3NavBaseUrl: String = BuildConfig.C3NAV_URL,

        // Session conversion functions
        private val toFeedbackUrl: Session.(String) -> String = { urlTemplate ->
            FeedbackUrlComposer(this, urlTemplate).getFeedbackUrl()
        },
        private val toPlainText: Session.() -> String = {
            SimpleSessionFormat.format(this)
        },
        private val toJson: Session.() -> String = {
            JsonSessionFormat.format(this)
        },
        private val toC3NavRoomName: Session.() -> String = {
            RoomForC3NavConverter.convert(this.room)
        }

) {

    interface ViewActionHandler {
        fun openFeedback(uri: Uri)
        fun shareAsPlainText(formattedSessions: String)
        fun shareAsJson(formattedSessions: String)
        fun addToCalendar(session: Session)
        fun showAlarmTimePicker()
        fun deleteAlarm(session: Session)
        fun navigateToRoom(uri: Uri)
        fun closeDetails()
        fun refreshUI()
    }

    val abstractFont = Font.Roboto.Bold
    val descriptionFont = Font.Roboto.Regular
    val linksFont = Font.Roboto.Regular
    val linksSectionFont = Font.Roboto.Bold
    val sessionOnlineFont = Font.Roboto.Regular
    val sessionOnlineSectionFont = Font.Roboto.Black
    val speakersFont = Font.Roboto.Black
    val subtitleFont = Font.Roboto.Light
    val titleFont = Font.Roboto.BoldCondensed

    fun onOptionsMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.menu_item_feedback -> {
            val uri = session.toFeedbackUrl(sessionFeedbackUrlTemplate).toUri()
            viewActionHandler.openFeedback(uri)
            true
        }
        R.id.menu_item_share_session,
        R.id.menu_item_share_session_text -> {
            val formattedSession = session.toPlainText()
            viewActionHandler.shareAsPlainText(formattedSession)
            true
        }
        R.id.menu_item_share_session_json -> {
            val formattedSessions = session.toJson()
            viewActionHandler.shareAsJson(formattedSessions)
            true
        }
        R.id.menu_item_add_to_calendar -> {
            viewActionHandler.addToCalendar(session)
            true
        }
        R.id.menu_item_flag_as_favorite -> {
            session.highlight = true // Required: Update property because refreshUI refers to its value!
            repository.updateHighlight(session)
            repository.notifyHighlightsChanged()
            viewActionHandler.refreshUI()
            true
        }
        R.id.menu_item_unflag_as_favorite -> {
            session.highlight = false // Required: Update property because refreshUI refers to its value!
            repository.updateHighlight(session)
            repository.notifyHighlightsChanged()
            viewActionHandler.refreshUI()
            true
        }
        R.id.menu_item_set_alarm -> {
            viewActionHandler.showAlarmTimePicker()
            true
        }
        R.id.menu_item_delete_alarm -> {
            viewActionHandler.deleteAlarm(session)
            viewActionHandler.refreshUI()
            true
        }
        R.id.menu_item_close_session_details -> {
            viewActionHandler.closeDetails()
            true
        }
        R.id.menu_item_navigate -> {
            val roomName = session.toC3NavRoomName()
            val uri = "$c3NavBaseUrl$roomName".toUri()
            viewActionHandler.navigateToRoom(uri)
            true
        }
        else -> false
    }


}
