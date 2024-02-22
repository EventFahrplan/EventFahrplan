package nerd.tuxmobil.fahrplan.congress.calendar

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import org.junit.jupiter.api.Test

/**
 * Covers [CalendarDescriptionComposer.getCalendarDescription].
 * Does not cover [MarkdownConversion] nor [SessionUrlComposition].
 */
class CalendarDescriptionComposerTest {

    @Test
    fun `getCalendarDescription returns session online`() {
        val session = createSession()
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns subtitle and session online`() {
        val session = createSession(subtitle = "Lorem ipsum dolor")
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Lorem ipsum dolor

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns speakers and session online`() {
        val session = createSession(speakers = listOf("Ada Lovelace", "Albert Einstein"))
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Ada Lovelace, Albert Einstein

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns abstract and session online`() {
        val session = createSession(abstract = "Lorem ipsum dolor sit amet.")
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Lorem ipsum dolor sit amet.

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns description and session online`() {
        val session = createSession(description = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns wiki links`() {
        val session = createSession(links = "[https://c3voc.de](https://c3voc.de)," +
                "[https://events.ccc.de/congress/2017/wiki/index.php/Session:A/V_Angel_Meeting]" +
                "(https://events.ccc.de/congress/2017/wiki/index.php/Session:A/V_Angel_Meeting)")
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            <a href="https://c3voc.de">https://c3voc.de</a><br><a href="https://events.ccc.de/congress/2017/wiki/index.php/Session:A/V_Angel_Meeting">https://events.ccc.de/congress/2017/wiki/index.php/Session:A/V_Angel_Meeting</a>
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns links`() {
        val session = createSession(links = "[OpenStreetMap](https://openstreetmap.org)," +
                "[https://overpass-turbo.eu](https://overpass-turbo.eu)")
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            <a href="https://openstreetmap.org">OpenStreetMap</a><br><a href="https://overpass-turbo.eu">https://overpass-turbo.eu</a>

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns full session description`() {
        val session = createSession(
            subtitle = "Lorem ipsum dolor",
            speakers = listOf("Ada Lovelace", "Albert Einstein"),
            abstract = "Lorem ipsum dolor sit amet.",
            description = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            links = "[Engelsystem](https://engelsystem.de)"
        )
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Lorem ipsum dolor

            Ada Lovelace, Albert Einstein

            Lorem ipsum dolor sit amet.

            Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

            <a href="https://engelsystem.de">Engelsystem</a>

            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    @Test
    fun `getCalendarDescription returns session online with uninitialized session`() {
        val session = createSession(subtitle = null, speakers = emptyList(), abstract = null, description = null, links = null)
        assertThat(createComposer().getCalendarDescription(session)).isEqualTo("""
            Session online: https://events.ccc.de/congress/2021/Fahrplan/events/2342.html
            """.trimIndent())
    }

    private fun createComposer(): CalendarDescriptionComposer {
        return CalendarDescriptionComposer("Session online", sessionUrlComposition = FakeSessionUrlComposer())
    }

    private class FakeSessionUrlComposer : SessionUrlComposition {

        override fun getSessionUrl(session: Session): String {
            return "https://events.ccc.de/congress/2021/Fahrplan/events/${session.sessionId}.html"
        }

    }

    private fun createSession(
        subtitle: String? = "",
        speakers: List<String> = emptyList(),
        abstract: String? = "",
        description: String? = "",
        links: String? = ""
    ) = Session("2342").apply {
        this.subtitle = subtitle
        this.speakers = speakers
        this.abstractt = abstract
        this.description = description
        this.links = links
    }

}
