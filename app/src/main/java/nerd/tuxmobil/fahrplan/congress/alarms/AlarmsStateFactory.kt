package nerd.tuxmobil.fahrplan.congress.alarms

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.ZoneOffset

class AlarmsStateFactory(
    private val resourceResolving: ResourceResolving,
    private val formattingDelegate: FormattingDelegate = DateFormatterDelegate,
) {
    /**
     * Delegate to get a formatted date/time.
     */
    fun interface FormattingDelegate {
        fun getFormattedDateTimeShort(
            useDeviceTimeZone: Boolean,
            alarmTime: Long,
            timeZoneOffset: ZoneOffset?,
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
            timeZoneOffset: ZoneOffset?,
        ) = DateFormatter.newInstance(useDeviceTimeZone)
            .getFormattedDateTimeLong(alarmTime, timeZoneOffset)
    }

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
                val alarmOffset =
                    (found.startsAt.toMilliseconds() - alarm.startTime).toMinutes()
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
                    alarmOffsetInMin = alarmOffset,
                    alarmOffsetContentDescription = alarmOffsetContentDescription,
                    firesAt = alarm.startTime,
                    firesAtText = firesAtText,
                    firesAtContentDescription = firesAtContentDescription,
                    dayIndex = found.dayIndex,
                )
            }
    }

}

private fun Long.toMinutes() = (this / MILLISECONDS_OF_ONE_MINUTE).toInt()
