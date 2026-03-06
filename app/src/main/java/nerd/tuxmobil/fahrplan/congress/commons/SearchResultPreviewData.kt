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
        startsAt = SearchResultProperty("10:00", ""),
    ),
    SearchResult(
        id = "2",
        title = SearchResultProperty("Dolor sit amet", ""),
        speakerNames = SearchResultProperty("Ada Lovelace, Grace Hopper, Alan Turing", ""),
        startsAt = SearchResultProperty("12:00", ""),
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
        startsAt = SearchResultProperty("18:30", ""),
    ),
)
