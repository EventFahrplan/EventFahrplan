package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.livedata.SingleLiveEvent
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink
import org.threeten.bp.ZoneOffset

class SessionDetailsViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val alarmServices: AlarmServices,
    private val sessionFormatter: SessionFormatter,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val feedbackUrlComposer: FeedbackUrlComposer,
    private val sessionUrlComposition: SessionUrlComposition,
    private val roomForC3NavConverter: RoomForC3NavConverter,
    private val markdownConversion: MarkdownConversion,
    private val formattingDelegate: FormattingDelegate = DateFormattingDelegate(),
    private val c3NavBaseUrl: String

) : ViewModel() {

    /**
     * Delegate to get a formatted date/time.
     */
    interface FormattingDelegate {
        fun getFormattedDateTimeShort(useDeviceTimeZone: Boolean, dateUtc: Long, sessionTimeZoneOffset: ZoneOffset?): String
        fun getFormattedDateTimeLong(useDeviceTimeZone: Boolean, dateUtc: Long, sessionTimeZoneOffset: ZoneOffset?): String
    }

    /**
     * [DateFormatter] delegate handling calls to get a formatted date/time.
     * Do not introduce any business logic here because this class is not unit tested.
     */
    private class DateFormattingDelegate : FormattingDelegate {

        override fun getFormattedDateTimeShort(useDeviceTimeZone: Boolean, dateUtc: Long, sessionTimeZoneOffset: ZoneOffset?): String {
            return DateFormatter.newInstance(useDeviceTimeZone).getFormattedDateTimeShort(dateUtc, sessionTimeZoneOffset)
        }

        override fun getFormattedDateTimeLong(useDeviceTimeZone: Boolean, dateUtc: Long, sessionTimeZoneOffset: ZoneOffset?): String {
            return DateFormatter.newInstance(useDeviceTimeZone).getFormattedDateTimeLong(dateUtc, sessionTimeZoneOffset)
        }

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

    val selectedSessionParameter: LiveData<SelectedSessionParameter> = repository.selectedSession
        .map { it.toSelectedSessionParameter() }
        .asLiveData(executionContext.database)

    val openFeedBack = SingleLiveEvent<Uri>()
    val shareSimple = SingleLiveEvent<String>()
    val shareJson = SingleLiveEvent<String>()
    val addToCalendar = SingleLiveEvent<Session>()
    val setAlarm = SingleLiveEvent<Unit>()
    val navigateToRoom = SingleLiveEvent<Uri>()
    val closeDetails = SingleLiveEvent<Unit>()

    private fun Session.toSelectedSessionParameter(): SelectedSessionParameter {
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
        val formattedZonedDateTimeShort = formattingDelegate.getFormattedDateTimeShort(useDeviceTimeZone, dateUTC, timeZoneOffset)
        val formattedZonedDateTimeLong = formattingDelegate.getFormattedDateTimeLong(useDeviceTimeZone, dateUTC, timeZoneOffset)
        val formattedAbstract = markdownConversion.markdownLinksToHtmlLinks(abstractt)
        val formattedDescription = markdownConversion.markdownLinksToHtmlLinks(description)
        val linksHtml = sessionFormatter.getFormattedLinks(getLinks())
        val formattedLinks = markdownConversion.markdownLinksToHtmlLinks(linksHtml)
        val sessionUrl = sessionUrlComposition.getSessionUrl(this)
        val sessionLink = sessionFormatter.getFormattedUrl(sessionUrl)
        val isFeedbackUrlEmpty = feedbackUrlComposer.getFeedbackUrl(this).isEmpty()
        val isC3NavRoomNameEmpty = roomForC3NavConverter.convert(room).isEmpty()

        return SelectedSessionParameter(
            // Details content
            sessionId = sessionId,
            hasDateUtc = dateUTC > 0,
            formattedZonedDateTimeShort = formattedZonedDateTimeShort,
            formattedZonedDateTimeLong = formattedZonedDateTimeLong,
            title = title.orEmpty(),
            subtitle = subtitle.orEmpty(),
            speakerNames = formattedSpeakers,
            speakersCount = speakers.size,
            abstract = abstractt.orEmpty(),
            formattedAbstract = formattedAbstract,
            description = description.orEmpty(),
            formattedDescription = formattedDescription,
            roomName = room.orEmpty(),
            hasLinks = getLinks().isNotEmpty(),
            formattedLinks = formattedLinks,
            hasWikiLinks = getLinks().containsWikiLink(),
            sessionLink = sessionLink,
            // Options menu
            isFlaggedAsFavorite = highlight,
            hasAlarm = hasAlarm,
            isFeedbackUrlEmpty = isFeedbackUrlEmpty,
            isC3NavRoomNameEmpty = isC3NavRoomNameEmpty
        )
    }

    fun openFeedback() {
        loadSelectedSession { session ->
            val uri = feedbackUrlComposer.getFeedbackUrl(session).toUri()
            openFeedBack.postValue(uri)
        }
    }

    fun share() {
        loadSelectedSession { session ->
            val timeZoneId = repository.readMeta().timeZoneId
            simpleSessionFormat.format(session, timeZoneId).let { formattedSession ->
                shareSimple.postValue(formattedSession)
            }
        }
    }

    fun shareToChaosflix() {
        loadSelectedSession { session ->
            jsonSessionFormat.format(session).let { formattedSession ->
                shareJson.postValue(formattedSession)
            }
        }
    }

    fun addToCalendar() {
        loadSelectedSession { session ->
            addToCalendar.postValue(session)
        }
    }

    fun favorSession() {
        loadSelectedSession { session ->
            val favoredSession = Session(session).apply {
                highlight = true // Required: Update property because updateHighlight refers to its value!
            }
            repository.updateHighlight(favoredSession)
        }
    }

    fun unfavorSession() {
        loadSelectedSession { session ->
            val unfavoredSession = Session(session).apply {
                highlight = false // Required: Update property because updateHighlight refers to its value!
            }
            repository.updateHighlight(unfavoredSession)
        }
    }

    fun setAlarm() {
        setAlarm.postValue(Unit)
    }

    fun addAlarm(alarmTimesIndex: Int) {
        loadSelectedSession { session ->
            alarmServices.addSessionAlarm(session, alarmTimesIndex)
        }
    }

    fun deleteAlarm() {
        loadSelectedSession { session ->
            alarmServices.deleteSessionAlarm(session)
        }
    }

    fun navigateToRoom() {
        loadSelectedSession { session ->
            val c3navRoomName = roomForC3NavConverter.convert(session.room)
            val uri = "$c3NavBaseUrl$c3navRoomName".toUri()
            navigateToRoom.postValue(uri)
        }
    }

    fun closeDetails() {
        closeDetails.postValue(Unit)
    }

    private fun loadSelectedSession(onSessionLoaded: (Session) -> Unit) {
        viewModelScope.launch(executionContext.database) {
            onSessionLoaded(repository.loadSelectedSession())
        }
    }

}
