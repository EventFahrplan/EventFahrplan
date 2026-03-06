package nerd.tuxmobil.fahrplan.congress.commons

import kotlinx.collections.immutable.persistentListOf
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.search.SearchResultProperty

fun createSearchResultPreviewData() = persistentListOf(
    Separator(
        DaySeparatorProperty(
            value = "DAY 1 - 12/27/2024",
            contentDescription = "Day 1 - December 27, 2024",
        )
    ),
    SearchResult(
        id = "1",
        title = SearchResultProperty("Lorem ipsum dolor sit amet", ""),
        speakerNames = SearchResultProperty("Hedy Llamar", ""),
        languages = SearchResultProperty("en", ""),
        roomName = SearchResultProperty("Hall 1", ""),
        startsAt = SearchResultProperty("10:00", ""),
        endsAt = SearchResultProperty("12:00", ""),
        recordingOptOut = SearchResultProperty(true, "Without video recording"),
    ),
    SearchResult(
        id = "2",
        title = SearchResultProperty("Dolor sit amet", ""),
        speakerNames = SearchResultProperty("Ada Lovelace, Grace Hopper, Alan Turing", ""),
        languages = SearchResultProperty("de", ""),
        roomName = SearchResultProperty("Hall 1", ""),
        startsAt = SearchResultProperty("12:00", ""),
        endsAt = SearchResultProperty("14:00", ""),
        recordingOptOut = null,
    ),
    Separator(
        DaySeparatorProperty(
            value = "DAY 2 - 12/28/2024",
            contentDescription = "Day 2 - December 28, 2024",
        )
    ),
    SearchResult(
        id = "3",
        title = SearchResultProperty(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            ""
        ),
        speakerNames = SearchResultProperty("Jane Doe", ""),
        languages = SearchResultProperty("en", ""),
        roomName = SearchResultProperty("Hall 1", ""),
        startsAt = SearchResultProperty("18:30", ""),
        endsAt = SearchResultProperty("21:00", ""),
        recordingOptOut = SearchResultProperty(true, "Without video recording")
    ),
)
