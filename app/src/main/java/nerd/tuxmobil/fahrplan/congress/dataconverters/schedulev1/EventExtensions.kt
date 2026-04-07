@file:OptIn(ExperimentalUuidApi::class)

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.network.serialization.ParserTask
import info.metadude.android.eventfahrplan.network.temporal.DateParser
import info.metadude.kotlin.library.schedule.v1.models.Day
import info.metadude.kotlin.library.schedule.v1.models.Event
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoField
import kotlin.uuid.ExperimentalUuidApi
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun Event.toSessionNetworkModel(
    day: Day,
    roomGuid: String,
    roomIndex: Int,
) = SessionNetworkModel(
    sessionId = id.toString(),
    sessionGuid = guid.toString(),
    dayIndex = day.index,
    dateText = day.date.toString(),
    dateUTC = date.toInstant().toEpochMilli(),
    abstractt = abstractText.sanitize(),
    description = description.sanitize(),
    duration = Duration.ofMinutes(duration.toMinutes()),
    relativeStartTime = getRelativeStartTimeWithDayChangeAdjustment(day),
    roomName = room,
    roomGuid = roomGuid,
    roomIndex = roomIndex,
    speakers = persons.toDelimitedSpeakersString(),
    startTime = start.getMinutesOfDay(),
    title = title.sanitize(),
    subtitle = subtitle.sanitize(),
    track = track.sanitize(),
    type = type.sanitize(),
    language = language.sanitize(),
    url = url.sanitize(),
    links = links.toMarkdownLinks(),
    feedbackUrl = feedbackUrl?.sanitize()?.ifEmpty { null },
    timeZoneOffset = date.offset.totalSeconds,
    slug = slug.sanitize(),
    recordingLicense = recordingLicense.sanitize(),
    recordingOptOut = doNotRecord == true,
)

/**
 * If the session starts before the conference "day end" boundary (see [ParserTask.parseEvent]),
 * relative start is advanced by one day so ordering matches the XML schedule.
 *
 * In XML, [ParserTask] updates `dayChangeTime` when it sees a `<day>` tag (`end` attribute) and can
 * also update it when it sees `<conference>` (e.g. `day_change`), so the value in effect for an
 * event depends on the order of those tags in the file. This JSON path only uses
 * [Day.dayEnd] for the current day, which matches the usual case where each `<day>` overwrites the
 * boundary before events under that day are parsed, but not every exotic XML ordering.
 *
 * That gap is not a practical problem for normal Frab-compatible exports (conference block then
 * days, or each day resetting the boundary before its events). It could only affect relative
 * start if the XML file order leaves `dayChangeTime` in a state that no longer matches that day's
 * `end` attribute when events are read, or if `dayEnd` in JSON does not represent the same instant
 * as the corresponding `<day end="…">` in XML.
 */
fun Event.getRelativeStartTimeWithDayChangeAdjustment(day: Day): Duration {
    val startTime = start.getMinutesOfDay()
    val dayChangeTimeMinutes = DateParser.getDayChange(day.dayEnd.toString())
    if (startTime.toWholeMinutes().toInt() < dayChangeTimeMinutes) {
        return startTime.plus(Duration.ofDays(1))
    }
    return startTime
}

private fun LocalTime.getMinutesOfDay() =
    Duration.ofMinutes(getLong(ChronoField.MINUTE_OF_DAY))

