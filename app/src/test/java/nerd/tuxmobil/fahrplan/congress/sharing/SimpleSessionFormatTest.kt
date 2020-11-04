package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class SimpleSessionFormatTest {

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()

    private val session1 = Session("S1").apply {
        title = "A talk which changes your life"
        room = "Yellow pavilion"
        date = "2019-12-27T11:00:00+01:00"
        url = "https://example.com/2019/LD3FX9.html"
    }

    private val session2 = Session("S2").apply {
        title = "The most boring workshop ever"
        room = "Dark cellar"
        date = "2019-12-28T17:00:00+01:00"
        url = "https://example.com/2019/U28VSA.html"
    }

    private val session3 = Session("S3").apply {
        title = "Angel shifts planning"
        room = "Main hall"
        date = "2019-12-29T09:00:00+01:00"
        links = "https://events.ccc.de/congress/2019/wiki/index.php/Session:A/V_Angel_Meeting"
        url = "https://example.com/2019/U28VSA.html"
    }

    @Before
    fun setUp() {
        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
    }

    @After
    fun cleanUp() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun `format returns formatted multiline text for a session`() {
        assertThat(SimpleSessionFormat.format(session1)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019 11:00 GMT+01:00, Yellow pavilion

                https://example.com/2019/LD3FX9.html
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a wiki session`() {
        assertThat(SimpleSessionFormat.format(session3)).isEqualTo(
                """
                Angel shifts planning
                Sonntag, 29. Dezember 2019 09:00 GMT+01:00, Main hall
                """.trimIndent())
    }

    @Test
    fun `format returns null for an empty sessions list`() {
        assertThat(SimpleSessionFormat.format(emptyList())).isNull()
    }

    @Test
    fun `format returns separated multiline text for a single session`() {
        assertThat(SimpleSessionFormat.format(listOf(session1))).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019 11:00 GMT+01:00, Yellow pavilion

                https://example.com/2019/LD3FX9.html
                """.trimIndent())
    }

    @Test
    fun `format returns separated multiline text for multiple sessions`() {
        assertThat(SimpleSessionFormat.format(listOf(session1, session2))).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019 11:00 GMT+01:00, Yellow pavilion

                https://example.com/2019/LD3FX9.html

                ---

                The most boring workshop ever
                Samstag, 28. Dezember 2019 17:00 GMT+01:00, Dark cellar

                https://example.com/2019/U28VSA.html
                """.trimIndent())
    }

}
