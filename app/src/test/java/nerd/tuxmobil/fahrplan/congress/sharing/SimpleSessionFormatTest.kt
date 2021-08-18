package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneId
import java.util.Locale
import java.util.TimeZone

// Most tests here only pass when being executed in a JDK 9+ environment.
// See https://stackoverflow.com/questions/65732319/how-to-stabilize-flaky-datetimeformatteroflocalizeddatetime-test
class SimpleSessionFormatTest {

    private companion object {
        val NO_TIME_ZONE_ID: ZoneId? = null
        val TIME_ZONE_EUROPE_BERLIN: ZoneId = ZoneId.of("Europe/Berlin")
    }

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()

    private val session1 = Session("S1").apply {
        title = "A talk which changes your life"
        room = "Yellow pavilion"
        date = "2019-12-27T11:00:00+01:00"
        url = "https://example.com/2019/LD3FX9.html"
        slug = "LD3FX9"
    }

    private val session2 = Session("S2").apply {
        title = "The most boring workshop ever"
        room = "Dark cellar"
        date = "2019-12-28T17:00:00+01:00"
        url = "https://example.com/2019/U28VSA.html"
        slug = "U28VSA"
    }

    private val session3 = Session("S3").apply {
        title = "Angel shifts planning"
        room = "Main hall"
        date = "2019-12-29T09:00:00+01:00"
        links = "https://events.ccc.de/congress/2019/wiki/index.php/Session:A/V_Angel_Meeting"
        url = "https://example.com/2019/U28VSA.html"
        slug = "U28VSA"
    }

    private val session4 = Session("S4").apply {
        title = "Central european summer time"
        room = "Sunshine tent"
        date = "2019-09-01T16:00:00+02:00"
        url = "https://example.com/2019/U9SD23.html"
        slug = "U9SD23"
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
    fun `format returns formatted multiline text for a session without time zone name`() {
        assertThat(SimpleSessionFormat.format(session1, NO_TIME_ZONE_ID)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 GMT+01:00, Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a session with time zone name`() {
        assertThat(SimpleSessionFormat.format(session1, TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 MEZ (Europe/Berlin), Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a wiki session`() {
        assertThat(SimpleSessionFormat.format(session3, TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                Angel shifts planning
                Sonntag, 29. Dezember 2019, 09:00 MEZ (Europe/Berlin), Main hall
                """.trimIndent())
    }

    @Test
    fun `format returns null for an empty sessions list`() {
        assertThat(SimpleSessionFormat.format(emptyList(), NO_TIME_ZONE_ID)).isNull()
    }

    @Test
    fun `format returns separated multiline text for a single session`() {
        assertThat(SimpleSessionFormat.format(listOf(session1), TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 MEZ (Europe/Berlin), Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns separated multiline text for multiple sessions`() {
        assertThat(SimpleSessionFormat.format(listOf(session1, session2), TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 MEZ (Europe/Berlin), Yellow pavilion

                $expectedSession1Url

                ---

                The most boring workshop ever
                Samstag, 28. Dezember 2019, 17:00 MEZ (Europe/Berlin), Dark cellar

                $expectedSession2Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a session in central european summer time`() {
        assertThat(SimpleSessionFormat.format(session4, TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                Central european summer time
                Sonntag, 1. September 2019, 16:00 MESZ (Europe/Berlin), Sunshine tent

                $expectedSession4Url
                """.trimIndent())
    }

    private val expectedSession1Url = createSessionUrl(slug = "LD3FX9", url = "https://example.com/2019/LD3FX9.html")
    private val expectedSession2Url = createSessionUrl(slug = "U28VSA", url = "https://example.com/2019/U28VSA.html")
    private val expectedSession4Url = createSessionUrl(slug = "U9SD23", url = "https://example.com/2019/U9SD23.html")

    private fun createSessionUrl(slug: String, url: String) = if (isPentabarfConfigured()) createPentabarfUrl(slug) else url

    private fun isPentabarfConfigured() = ServerBackendType.PENTABARF.name == BuildConfig.SERVER_BACKEND_TYPE

    private fun createPentabarfUrl(slug: String) = String.format(BuildConfig.EVENT_URL, slug)

}
