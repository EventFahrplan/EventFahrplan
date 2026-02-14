package nerd.tuxmobil.fahrplan.congress.details

import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsParameter.SessionDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink

class SessionDetailsParameterFactory(
    private val repository: SessionDetailsRepository,
    private val markupLanguage: MarkupLanguage,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
    private val formattingDelegate: FormattingDelegate,
    private val markdownConversion: MarkdownConversion,
    private val sessionUrlComposition: SessionUrlComposition,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String,
) {

    fun createSessionDetailsParameters(
        session: Session,
    ): SessionDetails {
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
        val formattedZonedDateTimeShort = formattingDelegate.getFormattedDateTimeShort(
            useDeviceTimeZone = useDeviceTimeZone,
            moment = session.startsAt,
            timeZoneOffset = session.timeZoneOffset,
        )
        val formattedZonedDateTimeLong = formattingDelegate.getFormattedDateTimeLong(
            useDeviceTimeZone = useDeviceTimeZone,
            moment = session.startsAt,
            timeZoneOffset = session.timeZoneOffset,
        )
        val formattedSessionId = sessionPropertiesFormatting.getFormattedSessionId(session.sessionId)
        val formattedSpeakerNames = sessionPropertiesFormatting.getFormattedSpeakers(session)
        val linksHtml = sessionPropertiesFormatting.getFormattedLinks(session.links)
        val formattedLinks = markdownConversion.markdownLinksToHtmlLinks(linksHtml)
        val formattedRoomName = sessionPropertiesFormatting.getRoomName(
            roomName = session.roomName,
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName,
        )
        val sessionLink = when (session.links.containsWikiLink()) {
            true -> "" // Session link is one of the wiki links.
            false -> {
                val sessionUrl = sessionUrlComposition.getSessionUrl(session)
                sessionPropertiesFormatting.getFormattedUrl(sessionUrl)
            }
        }

        return SessionDetails(
            id = SessionDetailsProperty(
                value = formattedSessionId,
                contentDescription = contentDescriptionFormatting.getSessionIdContentDescription(session.sessionId),
            ),
            title = SessionDetailsProperty(
                value = session.title,
                contentDescription = contentDescriptionFormatting.getTitleContentDescription(session.title),
            ),
            subtitle = SessionDetailsProperty(
                value = session.subtitle,
                contentDescription = contentDescriptionFormatting.getSubtitleContentDescription(session.subtitle),
            ),
            speakerNames = SessionDetailsProperty(
                value = formattedSpeakerNames,
                contentDescription = contentDescriptionFormatting.getSpeakersContentDescription(session.speakers.count(), formattedSpeakerNames),
            ),
            languages = SessionDetailsProperty(
                value = session.language,
                contentDescription = contentDescriptionFormatting.getLanguageContentDescription(session.language),
            ),
            abstract = SessionDetailsProperty(
                value = getMarkupProperty(session.abstractt),
                contentDescription = session.abstractt,
            ),
            description = SessionDetailsProperty(
                value = getMarkupProperty(session.description),
                contentDescription = session.description,
            ),
            trackName = SessionDetailsProperty(
                value = session.track,
                contentDescription = session.track, // Content description (headline + track name) is merged in composable.
            ),
            links = SessionDetailsProperty(
                value = formattedLinks,
                contentDescription = formattedLinks,
            ),
            startsAt = SessionDetailsProperty(
                value = formattedZonedDateTimeShort,
                contentDescription = contentDescriptionFormatting.getStartTimeContentDescription(formattedZonedDateTimeLong),
            ),
            roomName = SessionDetailsProperty(
                value = formattedRoomName,
                contentDescription = contentDescriptionFormatting.getRoomNameContentDescription(formattedRoomName),
            ),
            sessionLink = sessionLink, // Content description (headline + URL) is merged in composable.
        )
    }

    fun getMarkupProperty(text: String) = when (markupLanguage) {
        MarkupLanguage.Html -> Html(text)
        MarkupLanguage.Markdown -> Markdown(text)
    }

}
