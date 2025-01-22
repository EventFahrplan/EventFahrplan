package nerd.tuxmobil.fahrplan.congress.commons

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import org.threeten.bp.ZoneOffset

/**
 * [DateFormatter] delegate to handle calls to get a formatted date/time.
 * Do not introduce any business logic here because this class is not unit tested.
 */
@Suppress("kotlin:S6516")
object DateFormatterDelegate : FormattingDelegate {

    override fun getFormattedDateTimeShort(
        useDeviceTimeZone: Boolean,
        alarmTime: Long,
        timeZoneOffset: ZoneOffset?,
    ) = DateFormatter
        .newInstance(useDeviceTimeZone)
        .getFormattedDateTimeShort(alarmTime, timeZoneOffset)

    override fun getFormattedDateTimeLong(
        useDeviceTimeZone: Boolean,
        dateUtc: Long,
        sessionTimeZoneOffset: ZoneOffset?,
    ) = DateFormatter
        .newInstance(useDeviceTimeZone)
        .getFormattedDateTimeLong(dateUtc, sessionTimeZoneOffset)

}
