package nerd.tuxmobil.fahrplan.congress.dataconverters

import android.support.annotation.VisibleForTesting
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.DayRange
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper

// Avoid conflicts with the IDs of the main schedule.
private const val SHIFT_ID_OFFSET = 300000;

fun Shift.toLectureAppModel(

        logging: Logging,
        virtualRoomName: String,
        dayRanges: List<DayRange>

) = Lecture("${SHIFT_ID_OFFSET + sID}").apply {
    abstractt = ""
    date = startsAtLocalDateString
    dateUTC = dateUtcMs
    day = oneBasedDayIndex(logging, dayRanges)
    description = descriptionText
    duration = shiftDuration // minutes
    relStartTime = minuteOfDay
    room = virtualRoomName
    speakers = "-"
    startTime = minuteOfDay // minutes since day start
    title = name
    subtitle = talkTitle
    track = virtualRoomName
    url = talkUrl
}

/**
 * Returns the day index (starting at 1) based on the start date and time of this shift.
 * If the start time is within the start and end range of a day then the day index is returned.
 */
@VisibleForTesting
fun Shift.oneBasedDayIndex(logging: Logging, dayRanges: List<DayRange>): Int {
    dayRanges.forEachIndexed { index, dayRange ->
        if (dayRange.contains(startsAt)) {
            logging.d(javaClass.name, "${dayRange.startsAt} <= $startsAt < ${dayRange.endsAt} -> $talkTitle")
            return index + 1
        }
    }
    throw IllegalStateException("Shift start time $startsAt (${startsAt.toEpochSecond()}) exceeds all day ranges.")
}

private val Shift.dateUtcMs
    get() = startsAt.toEpochSecond().milliseconds

@VisibleForTesting
val Shift.descriptionText: String
    get() {
        var text = ""
        if (locationName.isNotEmpty()) {
            text += locationName
        }
        if (locationUrl.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "<br />"
            }
            text += "<a href=\"$locationUrl\">$locationUrl</a>"
        }
        if (locationDescription.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "<br /><br />"
            }
            text += locationDescription
        }
        if (userComment.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "<br /><br />"
            }
            text += "<em>$userComment</em>"
        }
        return text
    }

private val Shift.minuteOfDay
    get() = DateHelper.getMinuteOfDay(startsAt)

private val Shift.shiftDuration
    get() = DateHelper.getDurationMinutes(startsAt, endsAt).toInt()

private val Shift.startsAtLocalDate
    get() = startsAt.toLocalDate()

private val Shift.startsAtLocalDateString
    get() = startsAtLocalDate.toString()

private val Long.milliseconds
    get() = this * 1000
