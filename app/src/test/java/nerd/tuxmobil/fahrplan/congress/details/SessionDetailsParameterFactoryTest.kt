package nerd.tuxmobil.fahrplan.congress.details

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.threeten.bp.ZoneOffset

class SessionDetailsParameterFactoryTest {

    private companion object {
        const val SAMPLE_SESSION_URL = "https://conference.net/program/famous-talk.html"
    }

    @Test
    fun `createSessionDetailsParameters returns session details parameters`() {
        val factory = SessionDetailsParameterFactory(
            repository = mock(),
            markupLanguage = MarkupLanguage.Markdown,
            sessionPropertiesFormatting = FakeSessionPropertiesFormatting(),
            contentDescriptionFormatting = EmptyContentDescriptionFormatter(),
            formattingDelegate = FakeFormattingDelegate(),
            markdownConversion = FakeMarkdownConversion(),
            sessionUrlComposition = FakeSessionUrlComposition(),
            defaultEngelsystemRoomName = "Engelshifts",
            customEngelsystemRoomName = "Trollshifts",
        )
        val parameters = factory.createSessionDetailsParameters(
            Session(
                sessionId = "S1",
                title = "Session title",
                subtitle = "Session subtitle",
                speakers = listOf("Jane Doe", "John Doe"),
                abstractt = "Session abstract",
                description = "Session description",
                track = "Session track",
                links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)",
                dateUTC = 100,
                roomName = "Main hall",
                url = """<a href="$SAMPLE_SESSION_URL">$SAMPLE_SESSION_URL</a>""",
            )
        )
        assertThat(parameters.id).isEqualTo(SessionDetailsProperty("ID: 2342", ""))
        assertThat(parameters.title).isEqualTo(SessionDetailsProperty("Session title", ""))
        assertThat(parameters.subtitle).isEqualTo(SessionDetailsProperty("Session subtitle", ""))
        assertThat(parameters.speakerNames).isEqualTo(SessionDetailsProperty("Jane Doe, John Doe", ""))
        assertThat(parameters.abstract).isEqualTo(SessionDetailsProperty(Markdown("Session abstract"), "Session abstract"))
        assertThat(parameters.description).isEqualTo(SessionDetailsProperty(Markdown("Session description"), "Session description"))
        assertThat(parameters.trackName).isEqualTo(SessionDetailsProperty("Session track", "Session track"))
        assertThat(parameters.links).isEqualTo(
            SessionDetailsProperty(
                """<a href="https://www.voc.com/projects/">VOC projects</a>,<a href="https://poc.com/QXut1XBymAk">POC</a>""",
                """<a href="https://www.voc.com/projects/">VOC projects</a>,<a href="https://poc.com/QXut1XBymAk">POC</a>""",
            )
        )
        assertThat(parameters.startsAt).isEqualTo(SessionDetailsProperty("01.11.2021 13:00", ""))
        assertThat(parameters.roomName).isEqualTo(SessionDetailsProperty("Main hall", ""))
        assertThat(parameters.sessionLink).isEqualTo(SAMPLE_SESSION_URL)
    }

    private class FakeFormattingDelegate : FormattingDelegate {

        override fun getFormattedTimeShort(
            useDeviceTimeZone: Boolean,
            moment: Moment,
            timeZoneOffset: ZoneOffset?,
        ) = throw NotImplementedError("Not needed for this test.")

        override fun getFormattedDateShort(
            useDeviceTimeZone: Boolean,
            moment: Moment,
            timeZoneOffset: ZoneOffset?,
        ) = throw NotImplementedError("Not needed for this test.")

        override fun getFormattedDateLong(
            useDeviceTimeZone: Boolean,
            moment: Moment,
            timeZoneOffset: ZoneOffset?,
        ) = throw NotImplementedError("Not needed for this test.")

        override fun getFormattedDateTimeShort(
            useDeviceTimeZone: Boolean,
            moment: Moment,
            timeZoneOffset: ZoneOffset?,
        ) = "01.11.2021 13:00"

        override fun getFormattedDateTimeLong(
            useDeviceTimeZone: Boolean,
            moment: Moment,
            timeZoneOffset: ZoneOffset?,
        ) = ""
    }

    private class FakeMarkdownConversion : MarkdownConversion {
        override fun markdownLinksToHtmlLinks(markdown: String) =
            """<a href="https://www.voc.com/projects/">VOC projects</a>,<a href="https://poc.com/QXut1XBymAk">POC</a>"""

        override fun markdownLinksToPlainTextLinks(markdown: String) = ""
    }

    private class FakeSessionUrlComposition : SessionUrlComposition {
        override fun getSessionUrl(session: Session) = ""
    }

    private class FakeSessionPropertiesFormatting : SessionPropertiesFormatting {
        override fun getFormattedSessionId(id: String) = "ID: 2342"
        override fun getFormattedLinks(links: String) = ""
        override fun getFormattedUrl(url: String) = SAMPLE_SESSION_URL
        override fun getFormattedSpeakers(session: Session) = "Jane Doe, John Doe"
        override fun getLanguageText(session: Session) = ""
        override fun getRoomName(
            roomName: String,
            defaultEngelsystemRoomName: String,
            customEngelsystemRoomName: String,
        ) = "Main hall"
    }

    private class EmptyContentDescriptionFormatter : ContentDescriptionFormatting {
        override fun getSessionIdContentDescription(sessionId: String) = ""
        override fun getDurationContentDescription(duration: Duration) = ""
        override fun getTitleContentDescription(title: String) = ""
        override fun getSubtitleContentDescription(subtitle: String) = ""
        override fun getRoomNameContentDescription(roomName: String) = ""
        override fun getSpeakersContentDescription(
            speakersCount: Int,
            formattedSpeakerNames: String,
        ) = ""

        override fun getTrackNameContentDescription(trackName: String) = ""
        override fun getLanguageContentDescription(languageCode: String) = ""
        override fun getStartTimeContentDescription(startTimeText: String) = ""
        override fun getStateContentDescription(session: Session, useDeviceTimeZone: Boolean) = ""
        override fun getDaySeparatorContentDescription(dayIndex: Int, formattedDate: String) = ""
    }

}
