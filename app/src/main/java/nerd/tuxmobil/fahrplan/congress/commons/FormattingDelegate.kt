package nerd.tuxmobil.fahrplan.congress.commons

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.threeten.bp.ZoneOffset

/**
 * Delegate to get a formatted date/time.
 */
interface FormattingDelegate {

    fun getFormattedTimeShort(
        useDeviceTimeZone: Boolean,
        moment: Moment,
        timeZoneOffset: ZoneOffset?,
    ): String

    fun getFormattedDateShort(
        useDeviceTimeZone: Boolean,
        moment: Moment,
        timeZoneOffset: ZoneOffset?,
    ): String

    fun getFormattedDateLong(
        useDeviceTimeZone: Boolean,
        moment: Moment,
        timeZoneOffset: ZoneOffset?,
    ): String

    fun getFormattedDateTimeShort(
        useDeviceTimeZone: Boolean,
        moment: Moment,
        timeZoneOffset: ZoneOffset?,
    ): String

    fun getFormattedDateTimeLong(
        useDeviceTimeZone: Boolean,
        moment: Moment,
        timeZoneOffset: ZoneOffset?,
    ): String

}
