package nerd.tuxmobil.fahrplan.congress.favorites

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.Separator
import nerd.tuxmobil.fahrplan.congress.search.SearchResultProperty
import nerd.tuxmobil.fahrplan.congress.search.TenseType
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE
import nerd.tuxmobil.fahrplan.congress.search.TenseType.PAST
import org.junit.jupiter.api.Test

class FirstFutureItemIndexTest {

    @Test
    fun `returns -1 for empty list`() {
        assertThat(firstFutureItemIndex(emptyList())).isEqualTo(-1)
    }

    @Test
    fun `returns -1 when all entries are separators`() {
        val parameters = listOf(
            createSeparator(),
            createSeparator(),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(-1)
    }

    @Test
    fun `returns -1 when all search results are PAST`() {
        val parameters = listOf(
            createResult("a", PAST),
            createResult("b", PAST),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(-1)
    }

    @Test
    fun `returns 0 when first search result is FUTURE`() {
        val parameters = listOf(
            createResult("a", FUTURE),
            createResult("b", PAST),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(0)
    }

    @Test
    fun `returns first matching index when list starts with separators and then future result`() {
        val parameters = listOf(
            createSeparator(),
            createResult("a", PAST),
            createResult("b", FUTURE),
            createResult("c", FUTURE),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(2)
    }

    @Test
    fun `returns first matching index when future result happens later on second day`() {
        val parameters = listOf(
            createResult("a", PAST),
            createResult("b", PAST),
            createSeparator(),
            createResult("c", PAST),
            createResult("d", FUTURE),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(4)
    }

    @Test
    fun `returns index of first future result when mixed with past results`() {
        val parameters = listOf(
            createResult("a", PAST),
            createResult("b", PAST),
            createResult("c", FUTURE),
            createResult("d", FUTURE),
        )
        assertThat(firstFutureItemIndex(parameters)).isEqualTo(2)
    }

    private fun createResult(
        id: String = "1",
        tenseType: TenseType = FUTURE,
    ) = SearchResult(
        id = id,
        title = SearchResultProperty("", ""),
        speakerNames = SearchResultProperty("", ""),
        languages = SearchResultProperty("", ""),
        roomName = SearchResultProperty("", ""),
        startsAt = SearchResultProperty("", "", tenseType),
        endsAt = SearchResultProperty("", "", tenseType),
        recordingOptOut = null,
    )

    private fun createSeparator() = Separator(
        daySeparator = DaySeparatorProperty("Day 1", ""),
    )

}
