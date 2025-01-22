package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class SearchQueryFilterTest {

    private val filter = SearchQueryFilter()

    @Test
    fun `filterAll returns empty list when sessions is empty and query is empty`() {
        val result = filter.filterAll(emptyList(), "")
        assertThat(result).isEqualTo(emptyList<String>())
    }

    @Test
    fun `filterAll returns empty list when sessions is empty and query is not empty`() {
        val result = filter.filterAll(emptyList(), "test")
        assertThat(result).isEqualTo(emptyList<String>())
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches sessionId`() {
        val result = filter.filterAll(listOf(Session("1056")), "56")
        assertThat(result).isEqualTo(listOf(Session("1056")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches title`() {
        val result = filter.filterAll(listOf(Session("1", title = "some title")), "title")
        assertThat(result).isEqualTo(listOf(Session("1", title = "some title")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches subtitle`() {
        val result = filter.filterAll(listOf(Session("1", subtitle = "some subtitle")), "subtitle")
        assertThat(result).isEqualTo(listOf(Session("1", subtitle = "some subtitle")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches abstractt`() {
        val result = filter.filterAll(listOf(Session("1", abstractt = "some abstract")), "abstract")
        assertThat(result).isEqualTo(listOf(Session("1", abstractt = "some abstract")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches description`() {
        val result = filter.filterAll(listOf(Session("1", description = "some description")), "description")
        assertThat(result).isEqualTo(listOf(Session("1", description = "some description")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches track`() {
        val result = filter.filterAll(listOf(Session("1", track = "some track")), "track")
        assertThat(result).isEqualTo(listOf(Session("1", track = "some track")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches roomName`() {
        val result = filter.filterAll(listOf(Session("1", roomName = "some room name")), "room name")
        assertThat(result).isEqualTo(listOf(Session("1", roomName = "some room name")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches links`() {
        val result = filter.filterAll(listOf(Session("1", links = "https:/example.com")), "example")
        assertThat(result).isEqualTo(listOf(Session("1", links = "https:/example.com")))
    }

    @Test
    fun `filterAll returns list with session when query at least partially matches speakers`() {
        val result = filter.filterAll(listOf(Session("1", speakers = listOf("Jane Doe", "John Doe"))), "Jane")
        assertThat(result).isEqualTo(listOf(Session("1", speakers = listOf("Jane Doe", "John Doe"))))
    }

}
