package nerd.tuxmobil.fahrplan.congress.commons

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting

class DaySeparatorFactory(
    private val resourceResolving: ResourceResolving,
    private val formattingDelegate: FormattingDelegate,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
) {

    fun createDaySeparatorText(dayIndex: Int, session: Session, useDeviceTimeZone: Boolean): String {
        val formattedDate = formattingDelegate.getFormattedDateShort(
            useDeviceTimeZone,
            session.startsAt,
            session.timeZoneOffset,
        )
        return resourceResolving.getString(R.string.day_separator, dayIndex, formattedDate)
    }

    fun createDaySeparatorContentDescription(dayIndex: Int, session: Session, useDeviceTimeZone: Boolean): String {
        val formattedDate = formattingDelegate.getFormattedDateLong(
            useDeviceTimeZone,
            session.startsAt,
            session.timeZoneOffset,
        )
        return contentDescriptionFormatting.getDaySeparatorContentDescription(dayIndex, formattedDate)
    }

}
