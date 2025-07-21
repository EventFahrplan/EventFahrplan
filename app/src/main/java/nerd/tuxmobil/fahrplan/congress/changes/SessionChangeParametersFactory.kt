package nerd.tuxmobil.fahrplan.congress.changes

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeParameter.Separator
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeParameter.SessionChange
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.NEW
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.UNCHANGED
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.None
import nerd.tuxmobil.fahrplan.congress.dataconverters.toVirtualDays
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting

class SessionChangeParametersFactory(
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
    private val daySeparatorFactory: DaySeparatorFactory,
    private val formattingDelegate: FormattingDelegate,
) : FormattingDelegate by formattingDelegate {

    fun createSessionChangeParameters(
        sessions: List<Session>,
        useDeviceTimeZone: Boolean,
    ): List<SessionChangeParameter> {
        val dash = resourceResolving.getString(R.string.dash)
        val parameters = mutableListOf<SessionChangeParameter>()
        sessions
            .toVirtualDays()
            .filter { it.sessions.isNotEmpty() }
            .forEach { virtualDay ->
                val dayIndex = virtualDay.index
                val session = virtualDay.sessions.first()
                parameters += Separator(
                    DaySeparatorProperty(
                        value = daySeparatorFactory.createDaySeparatorText(
                            dayIndex = dayIndex,
                            session = session,
                            useDeviceTimeZone = useDeviceTimeZone,
                        ),
                        contentDescription = daySeparatorFactory.createDaySeparatorContentDescription(
                            dayIndex = dayIndex,
                            session = session,
                            useDeviceTimeZone = useDeviceTimeZone,
                        )
                    )
                )
                virtualDay.sessions.forEach {
                    parameters += sessionChangeOf(it, dash, useDeviceTimeZone)
                }
            }
        return parameters
    }

    private fun sessionChangeOf(session: Session, dash: String, useDeviceTimeZone: Boolean): SessionChange {
        val dayText = getFormattedDateShort(useDeviceTimeZone, session.startsAt, session.timeZoneOffset)
        val startsAt = getFormattedTimeShort(useDeviceTimeZone, session.startsAt, session.timeZoneOffset)
        val duration = resourceResolving.getString(R.string.session_list_item_duration_text, session.duration.toWholeMinutes().toInt())
        val languages = sessionPropertiesFormatting.getLanguageText(session)
        val videoState = when {
            session.changedRecordingOptOut -> when {
                session.recordingOptOut -> Unavailable
                else -> Available
            }

            else -> None
        }
        val speakerNames = sessionPropertiesFormatting.getFormattedSpeakers(session)
        val title = if (session.changedTitle && session.title.isEmpty()) dash else session.title

        return SessionChange(
            id = session.sessionId,
            title = SessionChangeProperty(
                value = title,
                contentDescription = title,
                changeState = changeStateOf(session, session.changedTitle),
            ),
            subtitle = SessionChangeProperty(
                value = if (session.changedSubtitle && session.subtitle.isEmpty()) dash else session.subtitle,
                contentDescription = contentDescriptionFormatting
                    .getSubtitleContentDescription(session.subtitle),
                changeState = changeStateOf(session, session.changedSubtitle),
            ),
            videoRecordingState = SessionChangeProperty(
                value = videoState,
                contentDescription = "", // The VideoRecordingState drawable provides its own content description.
                changeState = changeStateOf(session, session.changedRecordingOptOut),
            ),
            speakerNames = SessionChangeProperty(
                value = if (session.changedSpeakers && session.speakers.isEmpty()) dash else speakerNames,
                contentDescription = contentDescriptionFormatting
                    .getSpeakersContentDescription(session.speakers.size, speakerNames),
                changeState = changeStateOf(session, session.changedSpeakers),
            ),
            dayText = SessionChangeProperty(
                value = dayText,
                contentDescription = dayText,
                changeState = changeStateOf(session, session.changedDayIndex),
            ),
            startsAt = SessionChangeProperty(
                value = startsAt,
                contentDescription = contentDescriptionFormatting
                    .getStartTimeContentDescription(startsAt),
                changeState = changeStateOf(session, session.changedStartTime),
            ),
            duration = SessionChangeProperty(
                value = duration,
                contentDescription = contentDescriptionFormatting
                    .getDurationContentDescription(session.duration),
                changeState = changeStateOf(session, session.changedDuration),
            ),
            roomName = SessionChangeProperty(
                value = session.roomName,
                contentDescription = contentDescriptionFormatting
                    .getRoomNameContentDescription(session.roomName),
                changeState = changeStateOf(session, session.changedRoomName),
            ),
            languages = SessionChangeProperty(
                value = if (session.changedLanguage && session.language.isEmpty()) dash else languages,
                contentDescription = if (session.changedLanguage && session.language.isEmpty()) {
                    resourceResolving.getString(R.string.session_list_item_language_removed_content_description)
                } else {
                    contentDescriptionFormatting.getLanguageContentDescription(languages)
                },
                changeState = changeStateOf(session, session.changedLanguage),
            ),
            isCanceled = session.changedIsCanceled,
        )
    }

    private fun changeStateOf(session: Session, propertyChanged: Boolean) = when {
        session.changedIsNew -> NEW
        session.changedIsCanceled -> CANCELED
        propertyChanged -> CHANGED
        else -> UNCHANGED
    }

}
