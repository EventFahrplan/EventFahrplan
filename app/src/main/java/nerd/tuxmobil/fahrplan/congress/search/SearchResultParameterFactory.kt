package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class SearchResultParameterFactory(
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatter: SessionPropertiesFormatter,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
    private val formattingDelegate: FormattingDelegate
) : FormattingDelegate by formattingDelegate {

    fun createSearchResults(sessions: List<Session>, useDeviceTimeZone: Boolean): List<SearchResult> {
        return sessions.map { createSearchResult(it, useDeviceTimeZone) }
    }

    private fun createSearchResult(session: Session, useDeviceTimeZone: Boolean): SearchResult {
        val dash = resourceResolving.getString(R.string.dash)
        val title = session.title.ifEmpty { dash }
        val formattedSpeakerNames = sessionPropertiesFormatter.getFormattedSpeakers(session)
        val speakers = if (session.speakers.isEmpty()) dash else formattedSpeakerNames
        val startsAtText = formattingDelegate.getFormattedDateTimeLong(
            useDeviceTimeZone,
            session.startsAt.toMilliseconds(),
            session.timeZoneOffset,
        )

        return SearchResult(
            id = session.sessionId,
            title = SearchResultProperty(
                value = title,
                contentDescription = contentDescriptionFormatting
                    .getTitleContentDescription(session.title),
            ),
            speakerNames = SearchResultProperty(
                value = speakers,
                contentDescription = contentDescriptionFormatting
                    .getSpeakersContentDescription(session.speakers.size, formattedSpeakerNames),
            ),
            startsAt = SearchResultProperty(
                value = startsAtText,
                contentDescription = contentDescriptionFormatting.getStartTimeContentDescription(startsAtText),
            ),
        )
    }

}
