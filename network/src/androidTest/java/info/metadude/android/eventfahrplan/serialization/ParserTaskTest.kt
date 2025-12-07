package info.metadude.android.eventfahrplan.serialization

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DateParser
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.Session
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser.OnParseCompleteListener
import info.metadude.android.eventfahrplan.network.serialization.ParserTask
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ParserTaskTest {

    private val task = ParserTask(NoOpLogging, NoOpParseCompleteListener)

    @Nested
    inner class Ccc38c3 {

        @Test
        fun parseMeta() {
            val actualMeta = task.parseMeta("schedule-38c3-20251206.xml")
            val expectedMeta = Meta(
                scheduleGenerator = null,
                httpHeader = HttpHeader(),
                numDays = 4,
                title = "38C3",
                subtitle = "",
                timeZoneName = "Europe/Berlin",
                version = "2025-03-04 21:21",
            )
            assertThat(actualMeta).isEqualTo(expectedMeta)
        }

        @Test
        fun parseSessions_2() {
            val sessions = task.parseSessions("schedule-38c3-20251206.xml")
            val expectedSession = Session(
                sessionId = "2",
                title = "38C3: Opening Ceremony",
                subtitle = "",
                slug = "38c3-38c3-opening-ceremony",
                url = "https://events.ccc.de/congress/2024/hub/de/event/38c3-opening-ceremony/",
                track = "CCC",
                type = "Ceremony",
                language = "en",
                abstractt = "Glad you could make it! Take a seat and buckle up for a ride through four days of chaotic adventures.",
                description = "This ceremony will prepare you for the 38C3 in all its glory, underground and above, hacks and trolls, art and radical ideas. Let's kick this thing off together!",
                speakers = "Gabriela Bogk;Senficon;Aline Blankertz",
                links = "",
                feedbackUrl = "https://fahrplan.events.ccc.de/congress/2024/fahrplan/talk/HQCCYH/feedback/",
                startTime = Duration.ofMinutes(630), // 10:30 = 10*60 + 30
                relativeStartTime = Duration.ofMinutes(630),
                duration = Duration.ofMinutes(30), // 00:30
                roomName = "Saal 1",
                roomGuid = "ba692ba3-421b-5371-8309-60acc34a3c05",
                roomIndex = 0,
                dayIndex = 1,
                dateText = "2024-12-27",
                dateUTC = DateParser.parseDateTime("2024-12-27T10:30:00+01:00"),
                timeZoneOffset = DateParser.parseTimeZoneOffset("2024-12-27T10:30:00+01:00"),
            )

            assertThat(sessions).isNotEmpty()
            val actualSession = sessions.single { session -> session.sessionId == "2" }
            assertThat(actualSession).isEqualTo(expectedSession)
        }

        @Test
        fun parseSessions_765() {
            val sessions = task.parseSessions("schedule-38c3-20251206.xml")
            val expectedSession = Session(
                sessionId = "765",
                title = "CTF: WTF?! - Capture The Flag für Einsteiger",
                subtitle = "",
                slug = "38c3-ctf-wtf-capture-the-flag-fr-einsteiger",
                url = "https://events.ccc.de/congress/2024/hub/de/event/ctf-wtf-capture-the-flag-fr-einsteiger/",
                track = "", // Empty track element
                type = "Talk 60 (45min +15 Q&A)", // XML entity &amp; decoded to &
                language = "de",
                abstractt = "Capture The Flag (CTF) für Einsteiger: Wie man legal \"hacken\" ueben kann, warum man das tun sollte und wo man anfaengt.", // XML entity &quot; decoded to "
                description = "\"Hacken\" ist längst nicht mehr nur Hobby. WTF? CTF!\n\n                    Was ist ein \"Capture The Flag\", wie passt das in die aktuelle Menge aus Security Buzzwords, welchen Nutzen kann ich daraus ziehen und wie fange ich an?\n                    Es werden ein paar einfache Plattformen und Veranstaltungen zum starten und üben gezeigt. Dem folgen Spielarten, Wege \"hacken\" zu lernen, und ein Ausblick auf berufliche Möglichkeiten.\n\n                    Der Vortrag richtet sich an Einsteiger die neue Herausforderungen suchen und ihr Wissen um IT-Sicherheit vertiefen wollen.", // XML entities &quot; decoded to "
                speakers = "hubertf",
                links = "",
                feedbackUrl = "https://cfp.cccv.de/38c3-community-stages/talk/VDLGBQ/feedback/",
                startTime = Duration.ofMinutes(810), // 13:30 = 13*60 + 30
                relativeStartTime = Duration.ofMinutes(810),
                duration = Duration.ofHours(1),
                roomName = "Stage HUFF",
                roomGuid = "53f436a0-705b-40eb-974f-2bfce8857b1b",
                roomIndex = 3, // Stage HUFF is the fourth unique room
                dayIndex = 1,
                dateText = "2024-12-27",
                dateUTC = DateParser.parseDateTime("2024-12-27T13:30:00+01:00"),
                timeZoneOffset = DateParser.parseTimeZoneOffset("2024-12-27T13:30:00+01:00"),
            )

            assertThat(sessions).isNotEmpty()
            val actualSession = sessions.single { session -> session.sessionId == "765" }
            assertThat(actualSession).isEqualTo(expectedSession)
        }

    }

    private fun ParserTask.parseMeta(fileName: String): Meta {
        parseSchedule(fileName)
        return meta
    }

    private fun ParserTask.parseSessions(fileName: String): List<Session> {
        parseSchedule(fileName)
        return sessions
    }

    private fun ParserTask.parseSchedule(fileName: String): ParserTask {
        parseFahrplan(getScheduleFromFile(fileName), "", "")
        return this
    }

    private fun getScheduleFromFile(name: String) = javaClass.classLoader
        ?.getResourceAsStream(name)
        ?.bufferedReader()
        ?.use { it.readText() }
        ?: error("Could not load $name")

}

private val NoOpLogging = object : Logging {
    override fun d(tag: String, message: String) = Unit
    override fun e(tag: String, message: String) = Unit
    override fun report(tag: String, message: String) = Unit
}

private val NoOpParseCompleteListener = object : OnParseCompleteListener {
    override fun onUpdateSessions(sessions: List<Session?>) = Unit
    override fun onUpdateMeta(meta: Meta) = Unit
    override fun onParseDone(isSuccess: Boolean, version: String) = Unit
}
