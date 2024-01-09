package nerd.tuxmobil.fahrplan.congress.dataconverters

import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_SECOND
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.toMoment
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.Duration

private const val LOG_TAG = "ShiftsExtensions"

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
    roomName = virtualRoomName
    speakers = emptyList()
    startTime = minuteOfDay  // minutes since day start
    title = talkTitle
    subtitle = locationName
    // Shift.timeZoneName is not mapped here. Using Meta.timeZoneName instead.
    track = typeName
    url = talkUrl
}

/**
 * Returns the day index (starting at 1) based on the start date and time of this shift.
 * If the start time is within the start and end range of a day then the day index is returned.
 */
@VisibleForTesting
fun Shift.oneBasedDayIndex(logging: Logging, dayRanges: List<DayRange>): Int {
    dayRanges.forEachIndexed { index, dayRange ->
        if (dayRange.contains(startsAtDate)) {
            logging.d(LOG_TAG, "${dayRange.startsAt} <= $startsAtDate < ${dayRange.endsAt} -> $talkTitle")
            return index + 1
        }
    }
    error("Shift start time $startsAtDate (${startsAtDate.toEpochSecond()}) exceeds all day ranges.")
}

private val Shift.dateUtcMs
    get() = startsAtDate.toEpochSecond().milliseconds

@VisibleForTesting
val Shift.descriptionText: String
    get() {
        var text = ""
        if (locationUrl.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "\n"
            }
            text += "<a href=\"$locationUrl\">$locationUrl</a>"
        }
        if (typeDescription.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "\n\n"
            }
            text += typeDescription
        }
        if (locationDescription.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "\n\n"
            }
            text += locationDescription
        }
        if (userComment.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text += "\n\n"
            }
            text += "_${userComment.trim()}_"
        }
        return text
    }

private val Shift.minuteOfDay
    get() = startsAtDate.toMoment().minuteOfDay

private val Shift.shiftDuration
    get() = Duration.between(startsAtDate, endsAtDate).toMinutes().toInt()

private val Shift.startsAtLocalDate
    get() = startsAtDate.toLocalDate()

private val Shift.startsAtLocalDateString
    get() = startsAtLocalDate.toString()

private val Long.milliseconds
    get() = this * MILLISECONDS_OF_ONE_SECOND
