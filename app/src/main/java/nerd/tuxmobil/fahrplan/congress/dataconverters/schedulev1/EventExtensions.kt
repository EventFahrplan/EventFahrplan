@file:OptIn(ExperimentalUuidApi::class)

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.commons.temporal.Duration
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

private fun LocalTime.getMinutesOfDay() =
    Duration.ofMinutes(getLong(ChronoField.MINUTE_OF_DAY))

