package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import androidx.core.net.toUri
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink
import org.threeten.bp.ZoneId

class SessionDetailsViewModel @JvmOverloads constructor(

        private val repository: AppRepository,
        val sessionId: String,
        private val viewActionHandler: ViewActionHandler,
        private val markdownConversion: MarkdownConversion = MarkdownConverter,

        // Session conversion parameters
        private val sessionFeedbackUrlTemplate: String = BuildConfig.SCHEDULE_FEEDBACK_URL,
        private val c3NavBaseUrl: String = BuildConfig.C3NAV_URL,

        // Session conversion functions
        private val toFeedbackUrl: Session.(String) -> String = { urlTemplate ->
            FeedbackUrlComposer(this, urlTemplate).getFeedbackUrl()
        },
        private val toPlainText: Session.(ZoneId?) -> String = { timeZoneId ->
            SimpleSessionFormat().format(this, timeZoneId)
        },
        private val toJson: Session.() -> String = {
            JsonSessionFormat().format(this)
        },
        private val toC3NavRoomName: Session.() -> String = {
            RoomForC3NavConverter().convert(this.room)
        },
        private val toFormattedZonedDateTime: Session.() -> String = {
            val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
            DateFormatter.newInstance(useDeviceTimeZone).getFormattedDateTimeShort(this.dateUTC, this.timeZoneOffset)
        },
        private val toHtmlLink: String.() -> String = {
            markdownConversion.markdownLinksToHtmlLinks(this)
        },
        private val toSessionUrl: Session.() -> String = {
            SessionUrlComposer().getSessionUrl(this)
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

    // Needs to be a "var" so it can be modified (highlight, hasAlarm).
    private var session: Session = repository.readSessionBySessionId(sessionId)

    private val timeZoneId = repository.readMeta().timeZoneId

    val hasDateUtc get() = session.dateUTC > 0
    val formattedZonedDateTime get() = session.toFormattedZonedDateTime()

    val isSessionIdEmpty get() = sessionId.isEmpty()

    val roomName get() = session.room ?: ""

    val title get() = session.title ?: ""

    val isSubtitleEmpty get() = subtitle.isEmpty()
    val subtitle get() = session.subtitle ?: ""

    val isSpeakersEmpty get() = speakers.isEmpty()
    val speakers get() = session.formattedSpeakers

    val isAbstractEmpty get() = session.abstractt.isNullOrEmpty()
    val formattedAbstract get() = session.abstractt.toHtmlLink()
    val abstractt get() = session.abstractt ?: ""

    val isDescriptionEmpty get() = session.description.isNullOrEmpty()
    val formattedDescription get() = session.description.toHtmlLink()
    val description get() = session.description ?: ""

    val isLinksEmpty get() = session.getLinks().isEmpty()
    val formattedLinks: String
        get() {
            val html = session.getLinks().replace("\\),".toRegex(), ")<br>")
            return html.toHtmlLink()
        }

    val hasWikiLinks get() = session.getLinks().containsWikiLink()

    val sessionLink: String
        get() {
            val url = session.toSessionUrl()
            return if (url.isEmpty()) "" else "<a href=\"$url\">$url</a>"
        }

    val isFlaggedAsFavorite get() = session.highlight

    fun hasAlarm() = session.hasAlarm
    fun setHasAlarm(hasAlarm: Boolean) {
        session.hasAlarm = hasAlarm
    }

    val isFeedbackUrlEmpty get() = feedbackUrl.isEmpty()
    private val feedbackUrl get() = session.toFeedbackUrl(sessionFeedbackUrlTemplate)

    val isC3NavRoomNameEmpty get() = c3NavRoomName.isEmpty()
    private val c3NavRoomName get() = session.toC3NavRoomName()

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
            val uri = feedbackUrl.toUri()
            viewActionHandler.openFeedback(uri)
            true
        }
        R.id.menu_item_share_session,
        R.id.menu_item_share_session_text -> {
            val formattedSession = session.toPlainText(timeZoneId)
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
            val uri = "$c3NavBaseUrl$c3NavRoomName".toUri()
            viewActionHandler.navigateToRoom(uri)
            true
        }
        else -> false
    }

}
