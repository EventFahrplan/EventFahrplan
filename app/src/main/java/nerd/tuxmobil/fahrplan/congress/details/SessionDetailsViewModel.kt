package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.SessionAlarmViewModelDelegate
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.dataconverters.toRoom
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.roomstates.RoomStateFormatting
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposition
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink

internal class SessionDetailsViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging,
    buildConfigProvision: BuildConfigProvision,
    alarmServices: AlarmServices,
    notificationHelper: NotificationHelper,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val feedbackUrlComposition: FeedbackUrlComposition,
    private val sessionUrlComposition: SessionUrlComposition,
    private val indoorNavigation: IndoorNavigation,
    private val markdownConversion: MarkdownConversion,
    private val formattingDelegate: FormattingDelegate,
    private val roomStateFormatting: RoomStateFormatting,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String,
    runsAtLeastOnAndroidTiramisu: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

) : ViewModel(), FormattingDelegate by formattingDelegate, RoomStateFormatting by roomStateFormatting {

    private companion object {
        const val LOG_TAG = "SessionDetailsViewModel"
    }

    private var sessionAlarmViewModelDelegate: SessionAlarmViewModelDelegate =
        SessionAlarmViewModelDelegate(
            viewModelScope,
            notificationHelper,
            alarmServices,
            runsAtLeastOnAndroidTiramisu,
        )

    val abstractFont = Font.Roboto.Bold
    val descriptionFont = Font.Roboto.Regular
    val linksFont = Font.Roboto.Regular
    val linksSectionFont = Font.Roboto.Bold
    val trackFont = Font.Roboto.Regular
    val trackSectionFont = Font.Roboto.Black
    val sessionOnlineFont = Font.Roboto.Regular
    val sessionOnlineSectionFont = Font.Roboto.Black
    val speakersFont = Font.Roboto.Black
    val subtitleFont = Font.Roboto.Light
    val titleFont = Font.Roboto.BoldCondensed

    val selectedSessionParameter: Flow<SelectedSessionParameter> = repository.selectedSession
        .map { it.toSelectedSessionParameter() }
        .map { it.customizeEngelsystemRoomName() } // Do not rename before preparing parameter!
        .flowOn(executionContext.database)

    private val mutableOpenFeedBack = Channel<Uri>()
    val openFeedBack = mutableOpenFeedBack.receiveAsFlow()
    private val mutableShareSimple = Channel<String>()
    val shareSimple = mutableShareSimple.receiveAsFlow()
    private val mutableShareJson = Channel<String>()
    val shareJson = mutableShareJson.receiveAsFlow()
    private val mutableAddToCalendar = Channel<Session>()
    val addToCalendar = mutableAddToCalendar.receiveAsFlow()
    private val mutableNavigateToRoom = Channel<Uri>()
    val navigateToRoom = mutableNavigateToRoom.receiveAsFlow()
    private val mutableCloseDetails = Channel<Unit>()
    val closeDetails = mutableCloseDetails.receiveAsFlow()

    val requestPostNotificationsPermission = sessionAlarmViewModelDelegate
        .requestPostNotificationsPermission

    val notificationsDisabled = sessionAlarmViewModelDelegate
        .notificationsDisabled

    val requestScheduleExactAlarmsPermission = sessionAlarmViewModelDelegate
        .requestScheduleExactAlarmsPermission

    val showAlarmTimePicker = sessionAlarmViewModelDelegate
        .showAlarmTimePicker

    private val mutableRoomStateMessage = MutableStateFlow(roomStateFormatting.getText(null))
    val roomStateMessage = mutableRoomStateMessage.asStateFlow()

    init {
        if (buildConfigProvision.enableFosdemRoomStates) {
            updateRoomState()
        }
    }

    private fun SelectedSessionParameter.customizeEngelsystemRoomName() = copy(
        roomName = sessionPropertiesFormatting.getRoomName(
            roomName = roomName,
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName,
        )
    )

    private fun Session.toSelectedSessionParameter(): SelectedSessionParameter {
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
        val formattedZonedDateTimeShort = formattingDelegate.getFormattedDateTimeShort(useDeviceTimeZone, dateUTC, timeZoneOffset)
        val formattedZonedDateTimeLong = formattingDelegate.getFormattedDateTimeLong(useDeviceTimeZone, dateUTC, timeZoneOffset)
        val formattedAbstract = markdownConversion.markdownLinksToHtmlLinks(abstractt)
        val formattedDescription = markdownConversion.markdownLinksToHtmlLinks(description)
        val linksHtml = sessionPropertiesFormatting.getFormattedLinks(links)
        val formattedLinks = markdownConversion.markdownLinksToHtmlLinks(linksHtml)
        val sessionUrl = sessionUrlComposition.getSessionUrl(this)
        val sessionLink = sessionPropertiesFormatting.getFormattedUrl(sessionUrl)
        val isFeedbackUrlEmpty = feedbackUrlComposition.getFeedbackUrl(this).isEmpty()
        val supportsIndoorNavigation = indoorNavigation.isSupported(this.toRoom())
        val isEngelshift = roomName == defaultEngelsystemRoomName
        val supportsFeedback = !isFeedbackUrlEmpty && !isEngelshift

        return SelectedSessionParameter(
            // Details content
            sessionId = sessionId,
            hasDateUtc = dateUTC > 0,
            formattedZonedDateTimeShort = formattedZonedDateTimeShort,
            formattedZonedDateTimeLong = formattedZonedDateTimeLong,
            title = title,
            subtitle = subtitle,
            speakerNames = sessionPropertiesFormatting.getFormattedSpeakers(this),
            speakersCount = speakers.size,
            abstract = abstractt,
            formattedAbstract = formattedAbstract,
            description = description,
            formattedDescription = formattedDescription,
            roomName = roomName,
            track = track,
            hasLinks = links.isNotEmpty(),
            formattedLinks = formattedLinks,
            hasWikiLinks = links.containsWikiLink(),
            sessionLink = sessionLink,
            // Options menu
            isFlaggedAsFavorite = isHighlight,
            hasAlarm = hasAlarm,
            supportsFeedback = supportsFeedback,
            supportsIndoorNavigation = supportsIndoorNavigation,
        )
    }

    fun openFeedback() {
        loadSelectedSession { session ->
            val uri = feedbackUrlComposition.getFeedbackUrl(session).toUri()
            mutableOpenFeedBack.sendOneTimeEvent(uri)
        }
    }

    fun share() {
        loadSelectedSession { session ->
            val timeZoneId = repository.readMeta().timeZoneId
            simpleSessionFormat.format(session, timeZoneId).let { formattedSession ->
                mutableShareSimple.sendOneTimeEvent(formattedSession)
            }
        }
    }

    fun shareToChaosflix() {
        loadSelectedSession { session ->
            jsonSessionFormat.format(session).let { formattedSession ->
                mutableShareJson.sendOneTimeEvent(formattedSession)
            }
        }
    }

    fun addToCalendar() {
        loadSelectedSession { session ->
            mutableAddToCalendar.sendOneTimeEvent(session)
        }
    }

    fun favorSession() {
        loadSelectedSession { session ->
            val favoredSession = session.copy(
                isHighlight = true // Required: Update property because updateHighlight refers to its value!
            )
            repository.updateHighlight(favoredSession)
        }
    }

    fun unfavorSession() {
        loadSelectedSession { session ->
            val unfavoredSession = session.copy(
                isHighlight = false // Required: Update property because updateHighlight refers to its value!
            )
            repository.updateHighlight(unfavoredSession)
        }
    }

    fun canAddAlarms(): Boolean {
        return sessionAlarmViewModelDelegate.canAddAlarms()
    }

    fun addAlarmWithChecks() {
        sessionAlarmViewModelDelegate.addAlarmWithChecks()
    }

    fun addAlarm(alarmTimesIndex: Int) {
        loadSelectedSession { session ->
            sessionAlarmViewModelDelegate.addAlarm(session, alarmTimesIndex)
        }
    }

    fun deleteAlarm() {
        loadSelectedSession { session ->
            sessionAlarmViewModelDelegate.deleteAlarm(session)
        }
    }

    fun navigateToRoom() {
        loadSelectedSession { session ->
            val room = session.toRoom()
            val uri = indoorNavigation.getUri(room)
            mutableNavigateToRoom.sendOneTimeEvent(uri)
        }
    }

    fun closeDetails() {
        mutableCloseDetails.sendOneTimeEvent(Unit)
    }

    private fun loadSelectedSession(onSessionLoaded: (Session) -> Unit) {
        launch {
            onSessionLoaded(repository.loadSelectedSession())
        }
    }

    private fun updateRoomState() {
        combine(repository.selectedSession, repository.roomStates) { session, result ->
            result
                .onSuccess { rooms ->
                    val state = rooms.singleOrNull { it.name.trim().equals(session.roomName, ignoreCase = true) }?.state
                    if (state == null) {
                        logging.e(LOG_TAG, """Error matching room names. Unknown room name: "${session.roomName}".""")
                    }
                    mutableRoomStateMessage.value = roomStateFormatting.getText(state)
                }
                .onFailure {
                    logging.e(LOG_TAG, "Error fetching room states: $it")
                    mutableRoomStateMessage.value = roomStateFormatting.getFailureText(it)
                }
        }.launchIn(viewModelScope)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
