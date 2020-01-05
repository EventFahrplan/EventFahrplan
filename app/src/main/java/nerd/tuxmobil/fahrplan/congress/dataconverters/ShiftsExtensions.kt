package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.models.DayRange

fun List<Shift>.toLectureAppModels(

        logging: Logging,
        virtualRoomName: String,
        dayRanges: List<DayRange>

) = map { it.toLectureAppModel(logging, virtualRoomName, dayRanges) }

/**
 * Returns a list of shifts which only contains shifts which are within the given day ranges extent.
 * Shifts with a start date which is before or after the day ranges are dropped.
 */
fun List<Shift>.cropToDayRangesExtent(dayRanges: List<DayRange>) =
        filter { dayRanges.any { dayRange -> dayRange.contains(it.startsAt) } }
