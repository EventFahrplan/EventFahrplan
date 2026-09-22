package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.LANGUAGE_DELIMITER
import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.TRANSLATIONS_DELIMITER
import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.kotlin.library.schedule.v2.models.Day
import info.metadude.kotlin.library.schedule.v2.models.Event
import info.metadude.kotlin.library.schedule.v2.models.LocalizableString
import info.metadude.kotlin.library.schedule.v2.models.LocalizableString.Text
import info.metadude.kotlin.library.schedule.v2.models.LocalizableString.Translations
import info.metadude.kotlin.library.schedule.v2.models.MetaEvent
import info.metadude.kotlin.library.schedule.v2.models.PlannedEvent
import info.metadude.kotlin.library.schedule.v2.models.RecordingMode.RECORD_BY_DEFAULT
import info.metadude.kotlin.library.schedule.v2.models.ReferenceType
import info.metadude.kotlin.library.schedule.v2.models.Room
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEvent
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoField
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

internal fun Event.toSessionNetworkModel(
    day: Day,
    room: Room,
    roomIndex: Int,
    trackName: String,
): SessionNetworkModel = when (this) {
    is MetaEvent -> toSessionNetworkModel()
    is PlannedEvent -> toSessionNetworkModel()
    is ScheduledEvent -> toSessionNetworkModel(
        day = day,
        room = room,
        roomIndex = roomIndex,
        trackName = trackName,
    )
}

private fun Event.toSessionNetworkModel() = SessionNetworkModel(
    sessionId = guid.toString(),
    sessionGuid = guid.toString(),
)

private fun ScheduledEvent.toSessionNetworkModel(
    day: Day,
    room: Room,
    roomIndex: Int,
    trackName: String,
) = SessionNetworkModel(
    sessionId = guid.toString(),
    sessionGuid = guid.toString(),
    abstractt = abstractText?.value.sanitize(),
    dateText = day.date.toString(),
    dateUTC = start.toInstant().toEpochMilli(),
    dayIndex = day.index,
    description = description?.value.sanitize(),
    duration = getDuration(),
    feedbackUrl = getFeedbackUrl(),
    language = getLanguagesText(),
    links = getLinks(),
    recordingOptOut = room.recordingOptOut(),
    roomGuid = room.guid.toString(),
    roomIndex = roomIndex,
    roomName = room.name.sanitize(),
    slug = slug.sanitize(),
    speakers = participants.toDelimitedSpeakersString(),
    startTime = start.getMinutesOfDay(),
    subtitle = subtitle.sanitize(),
    timeZoneOffset = start.offset.totalSeconds,
    title = title.sanitize(),
    track = trackName.sanitize(),
    type = type.toString().sanitize(),
    url = url.sanitize(),
)

private val LocalizableString.value
    get() = when (this) {
        is Text -> value
        is Translations -> values.map { it.value }.joinToString(TRANSLATIONS_DELIMITER)
    }

private fun ScheduledEvent.getDuration(): Duration {
    val startsAt = Moment.ofEpochMilli(start.toInstant().toEpochMilli())
    val endsAt = Moment.ofEpochMilli(end.toInstant().toEpochMilli())
    return startsAt.durationUntil(endsAt)
}

private fun ScheduledEvent.getFeedbackUrl(): String? =
    links.firstOrNull { it.type == ReferenceType.FEEDBACK }?.url

private fun ScheduledEvent.getLanguagesText() = languages
    ?.map { it.sanitize() }
    ?.filter { it.isNotEmpty() }
    ?.joinToString(LANGUAGE_DELIMITER)
    .orEmpty()

private fun ScheduledEvent.getLinks(): String {
    val references = attachments + links
    return references.filter { it.type != ReferenceType.FEEDBACK }.toMarkdownLinks()
}

private fun Room.recordingOptOut() = features.recording != RECORD_BY_DEFAULT

private fun OffsetDateTime.getMinutesOfDay() =
    Duration.ofMinutes(getLong(ChronoField.MINUTE_OF_DAY))
