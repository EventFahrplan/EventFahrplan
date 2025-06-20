package nerd.tuxmobil.fahrplan.congress.alarms

import info.metadude.android.eventfahrplan.commons.temporal.Duration.Companion.ZERO
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Session

class AlarmsStateFactory(
    private val resourceResolving: ResourceResolving,
    private val formattingDelegate: FormattingDelegate,
) : FormattingDelegate by formattingDelegate {

    fun createAlarmsState(
        alarms: List<Alarm>,
        sessions: List<Session>,
        useDeviceTimeZone: Boolean,
    ): List<SessionAlarmParameter> = alarms.mapNotNull { alarm ->
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
                val alarmOffset = alarm.startTime.durationUntil(found.startsAt)
                val alarmOffsetContentDescription = when (alarmOffset == ZERO) {
                    true -> resourceResolving.getString(R.string.alarms_item_alarm_time_zero_minutes_content_description)
                    false -> resourceResolving.getString(
                        R.string.alarms_item_alarm_time_minutes_content_description,
                        alarmOffset.toWholeMinutes(),
                    )
                }
                val firesAtText = getFormattedDateTimeLong(
                    useDeviceTimeZone,
                    alarm.startTime,
                    found.timeZoneOffset,
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
                    alarmOffsetInMin = alarmOffset.toWholeMinutes().toInt(),
                    alarmOffsetContentDescription = alarmOffsetContentDescription,
                    firesAt = alarm.startTime.toMilliseconds(),
                    firesAtText = firesAtText,
                    firesAtContentDescription = firesAtContentDescription,
                    dayIndex = found.dayIndex,
                )
            }
    }

}
