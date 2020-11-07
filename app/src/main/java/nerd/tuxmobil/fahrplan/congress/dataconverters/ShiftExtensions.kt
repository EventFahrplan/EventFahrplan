package nerd.tuxmobil.fahrplan.congress.dataconverters

import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.toMoment
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.Duration

// Avoid conflicts with the IDs of the main schedule.
private const val SHIFT_ID_OFFSET = 300000

fun Shift.toSessionAppModel(

        logging: Logging,
        virtualRoomName: String,
        dayRanges: List<DayRange>

) = Session("${SHIFT_ID_OFFSET + sID}").apply {
    abstractt = ""
    date = startsAtLocalDateString
    dateUTC = dateUtcMs
    day = oneBasedDayIndex(logging, dayRanges)
    description = descriptionText
    duration = shiftDuration // minutes
    relStartTime = minuteOfDay
    room = virtualRoomName
    speakers = "-"
    startTime = minuteOfDay  // minutes since day start
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
            logging.d(javaClass.simpleName, "${dayRange.startsAt} <= $startsAt < ${dayRange.endsAt} -> $talkTitle")
            return index + 1
        }
    }
    error("Shift start time $startsAt (${startsAt.toEpochSecond()}) exceeds all day ranges.")
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
    get() = startsAt.toMoment().minuteOfDay

private val Shift.shiftDuration
    get() = Duration.between(startsAt, endsAt).toMinutes().toInt()

private val Shift.startsAtLocalDate
    get() = startsAt.toLocalDate()

private val Shift.startsAtLocalDateString
    get() = startsAtLocalDate.toString()

private val Long.milliseconds
    get() = this * 1000
