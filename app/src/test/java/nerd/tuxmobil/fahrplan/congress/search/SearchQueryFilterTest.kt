package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotRecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.RecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinSpeakerNamesSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTitleSubtitleSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTrackNameSearchFilter
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

    @Test
    fun `IsFavoriteSearchFilter only returns starred sessions`() {
        val session1 = Session("1", title = "Session 1", isHighlight = false)
        val session2 = Session("2", title = "Session 2", isHighlight = true)
        val session3 = Session("3", title = "no match", isHighlight = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(IsFavoriteSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }

    @Test
    fun `IsFavoriteSearchFilter with empty query returns all starred sessions`() {
        val session1 = Session("1", title = "Session 1", isHighlight = false)
        val session2 = Session("2", title = "Session 2", isHighlight = true)
        val session3 = Session("3", title = "Session 3", isHighlight = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(IsFavoriteSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `NotFavoriteSearchFilter only returns not starred sessions`() {
        val session1 = Session("1", title = "Session 1", isHighlight = true)
        val session2 = Session("2", title = "Session 2", isHighlight = false)
        val session3 = Session("3", title = "no match", isHighlight = false)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(NotFavoriteSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }

    @Test
    fun `NotFavoriteSearchFilter with empty query returns not all starred sessions`() {
        val session1 = Session("1", title = "Session 1", isHighlight = true)
        val session2 = Session("2", title = "Session 2", isHighlight = false)
        val session3 = Session("3", title = "Session 3", isHighlight = false)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(NotFavoriteSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `HasAlarmSearchFilter only returns sessions with alarm`() {
        val session1 = Session("1", title = "Session 1", hasAlarm = false)
        val session2 = Session("2", title = "Session 2", hasAlarm = true)
        val session3 = Session("3", title = "no match", hasAlarm = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(HasAlarmSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }

    @Test
    fun `HasAlarmSearchFilter with empty query returns all sessions with alarm`() {
        val session1 = Session("1", title = "Session 1", hasAlarm = false)
        val session2 = Session("2", title = "Session 2", hasAlarm = true)
        val session3 = Session("3", title = "Session 3", hasAlarm = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(HasAlarmSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `NotRecordedSearchFilter only returns sessions that are not recorded`() {
        val session1 = Session("1", title = "Session 1", recordingOptOut = false)
        val session2 = Session("2", title = "Session 2", recordingOptOut = true)
        val session3 = Session("3", title = "no match", recordingOptOut = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(NotRecordedSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }

    @Test
    fun `NotRecordedSearchFilter with empty query returns all sessions that are not recorded`() {
        val session1 = Session("1", title = "Session 1", recordingOptOut = false)
        val session2 = Session("2", title = "Session 2", recordingOptOut = true)
        val session3 = Session("3", title = "Session 3", recordingOptOut = true)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(NotRecordedSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `RecordedSearchFilter only returns sessions that are recorded`() {
        val session1 = Session("1", title = "Session 1", recordingOptOut = true)
        val session2 = Session("2", title = "Session 2", recordingOptOut = false)
        val session3 = Session("3", title = "no match", recordingOptOut = false)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(RecordedSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }

    @Test
    fun `RecordedSearchFilter with empty query returns all sessions that are not recorded`() {
        val session1 = Session("1", title = "Session 1", recordingOptOut = true)
        val session2 = Session("2", title = "Session 2", recordingOptOut = false)
        val session3 = Session("3", title = "Session 3", recordingOptOut = false)
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(RecordedSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `WithinSpeakerNamesSearchFilter only returns sessions where at least one speaker name matches the query`() {
        val session1 = Session("1", title = "Query 1", speakers = listOf("Jane Doe"))
        val session2 = Session("2", title = "Query 2", speakers = listOf("Jane Doe", "Peter Query"))
        val session3 = Session("3", title = "no title match", speakers = listOf("QUERY", "Jane Doe"))
        val sessions = listOf(session1, session2, session3)
        val filters = setOf(WithinSpeakerNamesSearchFilter())

        val result = filter.filterAll(sessions, query = "query", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `WithinSpeakerNamesSearchFilter with empty query doesn't return any matches`() {
        val session1 = Session("1", title = "Session 1", speakers = listOf("Jane Doe"))
        val session2 = Session("2", title = "Session 2", speakers = listOf("Jane Doe", "Peter Query"))
        val sessions = listOf(session1, session2)
        val filters = setOf(WithinSpeakerNamesSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).isEmpty()
    }

    @Test
    fun `WithinTitleSubtitleSearchFilter only returns sessions where title or subtitle matches the query`() {
        val session1 = Session("1", title = "Query 1", subtitle = "other")
        val session2 = Session("2", title = "other", subtitle = "Query 2")
        val session3 = Session("3", title = "no title match", subtitle = "QUERY")
        val session4 = Session("4", title = "no match", subtitle = "no match")
        val sessions = listOf(session1, session2, session3, session4)
        val filters = setOf(WithinTitleSubtitleSearchFilter())

        val result = filter.filterAll(sessions, query = "query", filters)

        assertThat(result).containsExactly(session1, session2, session3)
    }

    @Test
    fun `WithinTitleSubtitleSearchFilter with empty query doesn't return any matches`() {
        val session1 = Session("1", title = "Session 1", subtitle = "Subtitle")
        val session2 = Session("2", title = "Session 2", subtitle = "Another subtitle")
        val sessions = listOf(session1, session2)
        val filters = setOf(WithinTitleSubtitleSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).isEmpty()
    }

    @Test
    fun `WithinTrackNameSearchFilter only returns sessions where track name matches the query`() {
        val session1 = Session("1", title = "Track", track = "Security")
        val session2 = Session("2", title = "Session", track = "Security Track")
        val session3 = Session("3", title = "Other", track = "TRACK")
        val session4 = Session("4", title = "no match", track = "Workshop")
        val sessions = listOf(session1, session2, session3, session4)
        val filters = setOf(WithinTrackNameSearchFilter())

        val result = filter.filterAll(sessions, query = "track", filters)

        assertThat(result).containsExactly(session2, session3)
    }

    @Test
    fun `WithinTrackNameSearchFilter with empty query doesn't return any matches`() {
        val session1 = Session("1", title = "Session 1", track = "Security")
        val session2 = Session("2", title = "Session 2", track = "Workshop")
        val sessions = listOf(session1, session2)
        val filters = setOf(WithinTrackNameSearchFilter())

        val result = filter.filterAll(sessions, query = "", filters)

        assertThat(result).isEmpty()
    }

    @Test
    fun `all provided SearchFilters must match`() {
        val session1 = Session("1", title = "Session 1", isHighlight = true, hasAlarm = false)
        val session2 = Session("2", title = "Session 2", isHighlight = true, hasAlarm = true)
        val session3 = Session("3", title = "Session 3", isHighlight = false, hasAlarm = true)
        val session4 = Session("4", title = "Session 4", isHighlight = false, hasAlarm = false)
        val sessions = listOf(session1, session2, session3, session4)
        val filters = setOf(IsFavoriteSearchFilter(), HasAlarmSearchFilter())

        val result = filter.filterAll(sessions, query = "session", filters)

        assertThat(result).containsExactly(session2)
    }
}

private fun SearchQueryFilter.filterAll(sessions: List<Session>, query: String): List<Session> {
    return filterAll(sessions, query, filters = emptySet())
}
