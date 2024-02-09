package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.DateParser
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneId
import java.util.Locale
import java.util.TimeZone

// Most tests here only pass when being executed in a JDK 9+ environment.
// See https://stackoverflow.com/questions/65732319/how-to-stabilize-flaky-datetimeformatteroflocalizeddatetime-test
class SimpleSessionFormatTest {

    private companion object {
        val NO_TIME_ZONE_ID: ZoneId? = null
        val TIME_ZONE_EUROPE_BERLIN: ZoneId = ZoneId.of("Europe/Berlin")
        const val NO_SOCIAL_MEDIA_HASHTAGS_HANDLES = ""
        const val SOCIAL_MEDIA_HASHTAGS_HANDLES = "#fahrplan #36c3"
    }

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()

    private val session1 = Session("S1").apply {
        title = "A talk which changes your life"
        roomName = "Yellow pavilion"
        date = "2019-12-27T11:00:00+01:00"
        dateUTC = DateParser.parseDateTime(date)
        url = "https://example.com/2019/LD3FX9.html"
        slug = "LD3FX9"
    }

    private val session2 = Session("S2").apply {
        title = "The most boring workshop ever"
        roomName = "Dark cellar"
        date = "2019-12-28T17:00:00+01:00"
        dateUTC = DateParser.parseDateTime(date)
        url = "https://example.com/2019/U28VSA.html"
        slug = "U28VSA"
    }

    private val session3 = Session("S3").apply {
        title = "Angel shifts planning"
        roomName = "Main hall"
        date = "2019-12-29T09:00:00+01:00"
        dateUTC = DateParser.parseDateTime(date)
        links = "https://events.ccc.de/congress/2019/wiki/index.php/Session:A/V_Angel_Meeting"
        url = "https://example.com/2019/U28VSA.html"
        slug = "U28VSA"
    }

    private val session4 = Session("S4").apply {
        title = "Central european summer time"
        roomName = "Sunshine tent"
        date = "2019-09-01T16:00:00+02:00"
        dateUTC = DateParser.parseDateTime(date)
        url = "https://example.com/2019/U9SD23.html"
        slug = "U9SD23"
    }

    @BeforeEach
    fun setUp() {
        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
    }

    @AfterEach
    fun cleanUp() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun `format returns formatted multiline text for a session without time zone name`() {
        assertThat(SimpleSessionFormat().format(session1, NO_TIME_ZONE_ID, NO_SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 GMT+01:00, Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a session with time zone name`() {
        assertThat(SimpleSessionFormat().format(session1, TIME_ZONE_EUROPE_BERLIN, NO_SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 MEZ (Europe/Berlin), Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a session without social media hashtags`() {
        assertThat(SimpleSessionFormat().format(session1, NO_TIME_ZONE_ID, NO_SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 GMT+01:00, Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a session with social media hashtags`() {
        assertThat(SimpleSessionFormat().format(session1, NO_TIME_ZONE_ID, SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 GMT+01:00, Yellow pavilion

                $expectedSession1Url

                #fahrplan #36c3
                """.trimIndent())
    }

    @Test
    fun `format returns formatted multiline text for a wiki session`() {
        assertThat(SimpleSessionFormat().format(session3, TIME_ZONE_EUROPE_BERLIN, NO_SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
                """
                Angel shifts planning
                Sonntag, 29. Dezember 2019, 09:00 MEZ (Europe/Berlin), Main hall
                """.trimIndent())
    }

    @Test
    fun `format returns null for an empty sessions list`() {
        assertThat(SimpleSessionFormat().format(emptyList(), NO_TIME_ZONE_ID)).isNull()
    }

    @Test
    fun `format returns separated multiline text for a single session`() {
        assertThat(SimpleSessionFormat().format(listOf(session1), TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
                """
                A talk which changes your life
                Freitag, 27. Dezember 2019, 11:00 MEZ (Europe/Berlin), Yellow pavilion

                $expectedSession1Url
                """.trimIndent())
    }

    @Test
    fun `format returns separated multiline text for multiple sessions`() {
        assertThat(SimpleSessionFormat().format(listOf(session1, session2), TIME_ZONE_EUROPE_BERLIN)).isEqualTo(
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
        assertThat(SimpleSessionFormat().format(session4, TIME_ZONE_EUROPE_BERLIN, NO_SOCIAL_MEDIA_HASHTAGS_HANDLES)).isEqualTo(
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
