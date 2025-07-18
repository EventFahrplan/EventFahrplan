package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.dataconverters.toVirtualDays
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting

class SearchResultParameterFactory(
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
    private val daySeparatorFactory: DaySeparatorFactory,
    private val formattingDelegate: FormattingDelegate,
) : FormattingDelegate by formattingDelegate {

    fun createSearchResults(sessions: List<Session>, useDeviceTimeZone: Boolean): List<SearchResultParameter> {
        val list = mutableListOf<SearchResultParameter>()
        sessions
            .toVirtualDays()
            .filter { it.sessions.isNotEmpty() }
            .forEach { virtualDay ->
                val dayIndex = virtualDay.index
                val session = virtualDay.sessions.first()
                list += Separator(
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
                    list += createSearchResult(it, useDeviceTimeZone)
                }
            }
        return list
    }

    private fun createSearchResult(session: Session, useDeviceTimeZone: Boolean): SearchResult {
        val dash = resourceResolving.getString(R.string.dash)
        val title = session.title.ifEmpty { dash }
        val formattedSpeakerNames = sessionPropertiesFormatting.getFormattedSpeakers(session)
        val speakers = if (session.speakers.isEmpty()) dash else formattedSpeakerNames
        val startsAtText = getFormattedDateTimeLong(
            useDeviceTimeZone,
            session.startsAt,
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
