package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import org.threeten.bp.ZoneOffset

internal class AlarmsViewModel(

    private val repository: AppRepository = AppRepository,
    private val executionContext: ExecutionContext,
    private val resourceResolving: ResourceResolving,
    private val alarmServices: AlarmServices,
    private val screenNavigation: ScreenNavigation,
    private val formattingDelegate: FormattingDelegate = DateFormatterDelegate

) : ViewModel() {

    /**
     * Delegate to get a formatted date/time.
     */
    fun interface FormattingDelegate {
        fun getFormattedDateTimeShort(
            useDeviceTimeZone: Boolean,
            alarmTime: Long,
            timeZoneOffset: ZoneOffset?
        ): String
    }

    /**
     * [DateFormatter] delegate to handle calls to get a formatted date/time.
     * Do not introduce any business logic here because this class is not unit tested.
     */
    @Suppress("kotlin:S6516")
    private object DateFormatterDelegate : FormattingDelegate {
        override fun getFormattedDateTimeShort(
            useDeviceTimeZone: Boolean,
            alarmTime: Long,
            timeZoneOffset: ZoneOffset?
        ) = DateFormatter.newInstance(useDeviceTimeZone)
            .getFormattedDateTimeLong(alarmTime, timeZoneOffset)
    }

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    private val mutableAlarmsState = MutableStateFlow<AlarmsState>(Loading)
    val alarmsState = mutableAlarmsState.asStateFlow()

    init {
        combine(repository.alarms, repository.sessions) { alarms, sessions ->
            mutableAlarmsState.value = Success(
                sessionAlarmParameters = alarms.mapNotNull { alarm ->
                    sessions
                        .find { session -> session.sessionId == alarm.sessionId }
                        ?.let { found ->
                            val titleContentDescription = resourceResolving.getString(
                                R.string.session_list_item_title_content_description,
                                found.title
                            )
                            val subtitleContentDescription = resourceResolving.getString(
                                R.string.session_list_item_subtitle_content_description,
                                found.subtitle
                            )
                            val alarmOffset =
                                (found.startTimeMilliseconds - alarm.startTime).toMinutes()
                            val alarmOffsetContentDescription = when (alarmOffset == 0) {
                                true -> resourceResolving.getString(R.string.alarms_item_alarm_time_zero_minutes_content_description)
                                false -> resourceResolving.getString(
                                    R.string.alarms_item_alarm_time_minutes_content_description,
                                    alarmOffset
                                )
                            }
                            val firesAtText = formattingDelegate.getFormattedDateTimeShort(
                                useDeviceTimeZone,
                                alarm.startTime,
                                found.timeZoneOffset
                            )
                            val firesAtContentDescription = resourceResolving.getString(
                                R.string.alarms_item_fires_at_content_description,
                                firesAtText
                            )

                            SessionAlarmParameter(
                                sessionId = found.sessionId,
                                title = found.title,
                                titleContentDescription = titleContentDescription,
                                subtitle = found.subtitle,
                                subtitleContentDescription = subtitleContentDescription,
                                alarmOffsetInMin = alarmOffset,
                                alarmOffsetContentDescription = alarmOffsetContentDescription,
                                firesAt = alarm.startTime,
                                firesAtText = firesAtText,
                                firesAtContentDescription = firesAtContentDescription,
                                dayIndex = found.day
                            )
                        }
                },
                onItemClick = ::navigateToSessionDetails,
                onDeleteItemClick = ::deleteSessionAlarm
            )
        }.launchIn(viewModelScope)
    }

    private fun navigateToSessionDetails(value: SessionAlarmParameter) {
        screenNavigation.navigateToSessionDetails(value.sessionId)
    }

    private fun deleteSessionAlarm(value: SessionAlarmParameter) {
        launch {
            repository.deleteAlarmForSessionId(value.sessionId)
            val alarm = SchedulableAlarm(
                day = value.dayIndex,
                sessionId = value.sessionId,
                sessionTitle = value.title,
                startTime = value.firesAt
            )
            alarmServices.discardSessionAlarm(alarm)
        }
    }

    fun onDeleteAllClick() {
        launch {
            val alarms = repository.readAlarms()
            alarms
                .map { it.toSchedulableAlarm() }
                .forEach { alarmServices.discardSessionAlarm(it) }
            if (alarms.isNotEmpty()) {
                repository.deleteAllAlarms()
            }
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun Long.toMinutes() = (this / MILLISECONDS_OF_ONE_MINUTE).toInt()

}
