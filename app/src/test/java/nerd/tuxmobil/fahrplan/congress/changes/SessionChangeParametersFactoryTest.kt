package nerd.tuxmobil.fahrplan.congress.changes

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.NEW
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.UNCHANGED
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.None
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

private const val SOME_DATE = "8/13/15"
private const val SOME_TIME = "5:15 PM"

class SessionChangeParametersFactoryTest {

    @Test
    fun `createSessionChangeParameters returns empty list when sessions is empty`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            ContentDescriptionFormatter(mock()),
            DateFormatterCallback,
        )
        val actual = factory.createSessionChangeParameters(emptyList(), numDays = 0, useDeviceTimeZone = true)
        assertThat(actual).isEmpty()
    }

    @Test
    fun `createSessionChangeParameters returns UNCHANGED parameter when session is unchanged`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(createUnchangedSession())
        val actual = factory.createSessionChangeParameters(sessions, numDays = 1, useDeviceTimeZone = true)
        val expected = SessionChangeParameter.SessionChange(
            id = "2342",
            title = SessionChangeProperty(
                value = "Title",
                contentDescription = "Title",
                changeState = UNCHANGED,
            ),
            subtitle = SessionChangeProperty(
                value = "Subtitle",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            videoRecordingState = SessionChangeProperty(
                value = None,
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            speakerNames = SessionChangeProperty(
                value = "Jane Doe, John Doe",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            dayText = SessionChangeProperty(
                value = SOME_DATE,
                contentDescription = SOME_DATE,
                changeState = UNCHANGED,
            ),
            startsAt = SessionChangeProperty(
                value = SOME_TIME,
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            duration = SessionChangeProperty(
                value = "30",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            roomName = SessionChangeProperty(
                value = "Main room",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            languages = SessionChangeProperty(
                value = "de, en",
                contentDescription = "",
                changeState = UNCHANGED,
            ),
            isCanceled = false,
        )
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `createSessionChangeParameters returns NEW parameter when session is new`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(createNewSession())
        val actual = factory.createSessionChangeParameters(sessions, numDays = 1, useDeviceTimeZone = true)
        val expected = SessionChangeParameter.SessionChange(
            id = "2342",
            title = SessionChangeProperty(
                value = "Title",
                contentDescription = "Title",
                changeState = NEW,
            ),
            subtitle = SessionChangeProperty(
                value = "Subtitle",
                contentDescription = "",
                changeState = NEW,
            ),
            videoRecordingState = SessionChangeProperty(
                value = None,
                contentDescription = "",
                changeState = NEW,
            ),
            speakerNames = SessionChangeProperty(
                value = "Jane Doe, John Doe",
                contentDescription = "",
                changeState = NEW,
            ),
            dayText = SessionChangeProperty(
                value = SOME_DATE,
                contentDescription = SOME_DATE,
                changeState = NEW,
            ),
            startsAt = SessionChangeProperty(
                value = SOME_TIME,
                contentDescription = "",
                changeState = NEW,
            ),
            duration = SessionChangeProperty(
                value = "30",
                contentDescription = "",
                changeState = NEW,
            ),
            roomName = SessionChangeProperty(
                value = "Main room",
                contentDescription = "",
                changeState = NEW,
            ),
            languages = SessionChangeProperty(
                value = "de, en",
                contentDescription = "",
                changeState = NEW,
            ),
            isCanceled = false,
        )
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `createSessionChangeParameters returns CANCELED parameter when session is canceled`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(createCanceledSession())
        val actual = factory.createSessionChangeParameters(sessions, numDays = 1, useDeviceTimeZone = true)
        val expected = SessionChangeParameter.SessionChange(
            id = "2342",
            title = SessionChangeProperty(
                value = "Title",
                contentDescription = "Title",
                changeState = CANCELED,
            ),
            subtitle = SessionChangeProperty(
                value = "Subtitle",
                contentDescription = "",
                changeState = CANCELED,
            ),
            videoRecordingState = SessionChangeProperty(
                value = None,
                contentDescription = "",
                changeState = CANCELED,
            ),
            speakerNames = SessionChangeProperty(
                value = "Jane Doe, John Doe",
                contentDescription = "",
                changeState = CANCELED,
            ),
            dayText = SessionChangeProperty(
                value = SOME_DATE,
                contentDescription = SOME_DATE,
                changeState = CANCELED,
            ),
            startsAt = SessionChangeProperty(
                value = SOME_TIME,
                contentDescription = "",
                changeState = CANCELED,
            ),
            duration = SessionChangeProperty(
                value = "30",
                contentDescription = "",
                changeState = CANCELED,
            ),
            roomName = SessionChangeProperty(
                value = "Main room",
                contentDescription = "",
                changeState = CANCELED,
            ),
            languages = SessionChangeProperty(
                value = "de, en",
                contentDescription = "",
                changeState = CANCELED,
            ),
            isCanceled = true,
        )
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `createSessionChangeParameters returns CHANGED parameter when session is changed`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(createChangedSession())
        val actual = factory.createSessionChangeParameters(sessions, numDays = 1, useDeviceTimeZone = true)
        val expected = SessionChangeParameter.SessionChange(
            id = "2342",
            title = SessionChangeProperty(
                value = "Title",
                contentDescription = "Title",
                changeState = CHANGED,
            ),
            subtitle = SessionChangeProperty(
                value = "Subtitle",
                contentDescription = "",
                changeState = CHANGED,
            ),
            videoRecordingState = SessionChangeProperty(
                value = Available,
                contentDescription = "",
                changeState = CHANGED,
            ),
            speakerNames = SessionChangeProperty(
                value = "Jane Doe, John Doe",
                contentDescription = "",
                changeState = CHANGED,
            ),
            dayText = SessionChangeProperty(
                value = SOME_DATE,
                contentDescription = SOME_DATE,
                changeState = CHANGED,
            ),
            startsAt = SessionChangeProperty(
                value = SOME_TIME,
                contentDescription = "",
                changeState = CHANGED,
            ),
            duration = SessionChangeProperty(
                value = "30",
                contentDescription = "",
                changeState = CHANGED,
            ),
            roomName = SessionChangeProperty(
                value = "Main room",
                contentDescription = "",
                changeState = CHANGED,
            ),
            languages = SessionChangeProperty(
                value = "de, en",
                contentDescription = "",
                changeState = CHANGED,
            ),
            isCanceled = false,
        )
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `createSessionChangeParameters returns CHANGED parameter with dashes when session is changed and empty`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(createChangedEmptySession())
        val actual = factory.createSessionChangeParameters(sessions, numDays = 1, useDeviceTimeZone = true)
        val expected = SessionChangeParameter.SessionChange(
            id = "2342",
            title = SessionChangeProperty(
                value = "-",
                contentDescription = "-",
                changeState = CHANGED,
            ),
            subtitle = SessionChangeProperty(
                value = "-",
                contentDescription = "",
                changeState = CHANGED,
            ),
            videoRecordingState = SessionChangeProperty(
                value = Unavailable,
                contentDescription = "",
                changeState = CHANGED,
            ),
            speakerNames = SessionChangeProperty(
                value = "-",
                contentDescription = "",
                changeState = CHANGED,
            ),
            dayText = SessionChangeProperty(
                value = SOME_DATE,
                contentDescription = SOME_DATE,
                changeState = CHANGED,
            ),
            startsAt = SessionChangeProperty(
                value = SOME_TIME,
                contentDescription = "",
                changeState = CHANGED,
            ),
            duration = SessionChangeProperty(
                value = "30",
                contentDescription = "",
                changeState = CHANGED,
            ),
            roomName = SessionChangeProperty(
                value = "",
                contentDescription = "",
                changeState = CHANGED,
            ),
            languages = SessionChangeProperty(
                value = "-",
                contentDescription = "Language information has been removed",
                changeState = CHANGED,
            ),
            isCanceled = false,
        )
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `createSessionChangeParameters returns day separator when session span two days`() {
        val factory = SessionChangeParametersFactory(
            CompleteResourceResolver,
            createSessionPropertiesFormatter(),
            createContentDescriptionFormatter(),
            DateFormatterCallback,
        )
        val sessions = listOf(
            createUnchangedSession(dayIndex = 0),
            createUnchangedSession(dayIndex = 1),
        )
        val actual = factory.createSessionChangeParameters(sessions, numDays = 2, useDeviceTimeZone = true)
        assertThat(actual).hasSize(3)
        assertThat(actual).contains(SessionChangeParameter.Separator("Day 1 ..."))
    }

}

private fun createUnchangedSession(dayIndex: Int = 0) = Session(
    sessionId = "2342",
    title = "Title",
    subtitle = "Subtitle",
    recordingOptOut = false,
    speakers = listOf("Jane Doe", "John Doe"),
    dateUTC = 1439478900000L,
    duration = 30,
    roomName = "Main room",
    language = "de, en",
    dayIndex = dayIndex,

    changedTitle = false,
    changedSubtitle = false,
    changedRecordingOptOut = false,
    changedSpeakers = false,
    changedStartTime = false,
    changedRoomName = false,
    changedDuration = false,
    changedLanguage = false,
    changedDayIndex = false,

    changedIsNew = false,
    changedIsCanceled = false,
)

private fun createNewSession() = Session(
    sessionId = "2342",
    title = "Title",
    subtitle = "Subtitle",
    recordingOptOut = false,
    speakers = listOf("Jane Doe", "John Doe"),
    dateUTC = 1439478900000L,
    duration = 30,
    roomName = "Main room",
    language = "de, en",

    changedTitle = false,
    changedSubtitle = false,
    changedRecordingOptOut = false,
    changedSpeakers = false,
    changedStartTime = false,
    changedRoomName = false,
    changedDuration = false,
    changedLanguage = false,
    changedDayIndex = false,

    changedIsNew = true,
    changedIsCanceled = false,
)

private fun createCanceledSession() = Session(
    sessionId = "2342",
    title = "Title",
    subtitle = "Subtitle",
    recordingOptOut = false,
    speakers = listOf("Jane Doe", "John Doe"),
    dateUTC = 1439478900000L,
    duration = 30,
    roomName = "Main room",
    language = "de, en",

    changedTitle = false,
    changedSubtitle = false,
    changedRecordingOptOut = false,
    changedSpeakers = false,
    changedStartTime = false,
    changedRoomName = false,
    changedDuration = false,
    changedLanguage = false,
    changedDayIndex = false,

    changedIsNew = false,
    changedIsCanceled = true,
)

private fun createChangedSession() = Session(
    sessionId = "2342",
    title = "Title",
    subtitle = "Subtitle",
    recordingOptOut = false,
    speakers = listOf("Jane Doe", "John Doe"),
    dateUTC = 1439478900000L,
    duration = 30,
    roomName = "Main room",
    language = "de, en",

    changedTitle = true,
    changedSubtitle = true,
    changedRecordingOptOut = true,
    changedSpeakers = true,
    changedStartTime = true,
    changedRoomName = true,
    changedDuration = true,
    changedLanguage = true,
    changedDayIndex = true,

    changedIsNew = false,
    changedIsCanceled = false,
)

private fun createChangedEmptySession() = Session(
    sessionId = "2342",
    title = "",
    subtitle = "",
    recordingOptOut = true,
    speakers = emptyList(),
    dateUTC = 1439478900000L,
    duration = 30,
    roomName = "",
    language = "",

    changedTitle = true,
    changedSubtitle = true,
    changedRecordingOptOut = true,
    changedSpeakers = true,
    changedStartTime = true,
    changedRoomName = true,
    changedDuration = true,
    changedLanguage = true,
    changedDayIndex = true,

    changedIsNew = false,
    changedIsCanceled = false,
)

private fun createSessionPropertiesFormatter(): SessionPropertiesFormatter {
    return SessionPropertiesFormatter()
}

private fun createContentDescriptionFormatter() = mock<ContentDescriptionFormatter> {
    on { getDurationContentDescription(anyOrNull()) } doReturn ""
    on { getTitleContentDescription(anyOrNull()) } doReturn ""
    on { getSubtitleContentDescription(anyOrNull()) } doReturn ""
    on { getRoomNameContentDescription(anyOrNull()) } doReturn ""
    on { getSpeakersContentDescription(anyOrNull(), anyOrNull()) } doReturn ""
    on { getTrackNameAndLanguageContentDescription(anyOrNull(), anyOrNull()) } doReturn ""
    on { getLanguageContentDescription(anyOrNull()) } doReturn ""
    on { getStartTimeContentDescription(anyOrNull()) } doReturn ""
    on { getStateContentDescription(anyOrNull(), anyOrNull()) } doReturn ""
}

private object DateFormatterCallback : (Boolean) -> DateFormatter {
    override fun invoke(useDeviceTimeZone: Boolean) = mock<DateFormatter> {
        on { getFormattedDate(anyOrNull(), anyOrNull()) } doReturn SOME_DATE
        on { getFormattedTime(anyOrNull(), anyOrNull()) } doReturn SOME_TIME
    }
}

private object CompleteResourceResolver : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.session_list_item_duration_text -> "30"
        R.string.day_separator -> "Day 1 ..."
        R.string.dash -> "-"
        R.string.session_list_item_language_removed_content_description -> "Language information has been removed"
        else -> fail("Unknown string id : $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        throw NotImplementedError("Not needed for this test.")
    }
}
