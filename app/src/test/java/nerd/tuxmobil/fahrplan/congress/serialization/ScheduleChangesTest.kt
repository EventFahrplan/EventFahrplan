package nerd.tuxmobil.fahrplan.congress.serialization

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.Companion.computeSessionsWithChangeFlags
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.threeten.bp.ZoneOffset

class ScheduleChangesTest {

    companion object {

        private fun scenario1Of(
            scenarioDescription: String,
            oldSessions: List<Session>,
            newSessions: List<Session>,
            expectedSessions: List<Session>,
            expectedOldCanceledSessions: List<Session>,
            expectedFoundNoteworthyChanges: Boolean,
        ) = Arguments.of(
            scenarioDescription,
            oldSessions,
            newSessions,
            expectedSessions,
            expectedOldCanceledSessions,
            expectedFoundNoteworthyChanges,
        )

        @Suppress("SameParameterValue")
        private fun scenario2Of(
            scenarioDescription: String,
            oldSessions: List<Session>,
            newSessions: List<Session>,
            expectedSessions: List<Session>,
            expectedOldCanceledSessions: List<Session>,
            expectedFoundNoteworthyChanges: Boolean,
            expectedFoundChanges: Boolean,
        ) = Arguments.of(
            scenarioDescription,
            oldSessions,
            newSessions,
            expectedSessions,
            expectedOldCanceledSessions,
            expectedFoundNoteworthyChanges,
            expectedFoundChanges,
        )

        @JvmStatic
        fun dataScenarios1() = listOf(
            scenario1Of(
                scenarioDescription = "Old and new sessions are empty",
                oldSessions = emptyList(),
                newSessions = emptyList(),
                expectedSessions = emptyList(),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
            ),
            scenario1Of(
                scenarioDescription = "Old sessions are canceled",
                oldSessions = listOf(Session("1").apply { changedIsCanceled = true }),
                newSessions = emptyList(),
                expectedSessions = emptyList(),
                expectedOldCanceledSessions = listOf(Session("1").apply {
                    changedIsCanceled = true
                }),
                expectedFoundNoteworthyChanges = false,
            ),
            scenario1Of(
                scenarioDescription = "New sessions are added",
                oldSessions = listOf(Session("canceled").apply { changedIsCanceled = true }),
                newSessions = listOf(Session("new").apply { changedIsCanceled = false }),
                expectedSessions = listOf(Session("new").apply { changedIsNew = true }),
                expectedOldCanceledSessions = listOf(Session("canceled").apply {
                    changedIsCanceled = true
                }),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Old and new sessions are the same",
                oldSessions = listOf(
                    Session("").apply {
                        title = "title"
                        subtitle = "subtitle"
                        speakers = listOf("speakers")
                        language = "language"
                        roomName = "room"
                        recordingOptOut = true
                        dayIndex = 3
                        startTime = 200
                        duration = 90
                    }
                ),
                newSessions = listOf(
                    Session("").apply {
                        title = "title"
                        subtitle = "subtitle"
                        speakers = listOf("speakers")
                        language = "language"
                        roomName = "room"
                        recordingOptOut = true
                        dayIndex = 3
                        startTime = 200
                        duration = 90
                    }
                ),
                expectedSessions = listOf(
                    Session("").apply {
                        title = "title"
                        subtitle = "subtitle"
                        speakers = listOf("speakers")
                        language = "language"
                        roomName = "room"
                        recordingOptOut = true
                        dayIndex = 3
                        startTime = 200
                        duration = 90
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
            ),
            scenario1Of(
                scenarioDescription = "Titles differ",
                oldSessions = listOf(Session("1").apply { title = "Old title" }),
                newSessions = listOf(Session("1").apply { title = "New title" }),
                expectedSessions = listOf(Session("1").apply {
                    title = "New title"
                    changedTitle = true
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Subtitles differ",
                oldSessions = listOf(Session("1").apply { subtitle = "Old subtitle" }),
                newSessions = listOf(Session("1").apply { subtitle = "New subtitle" }),
                expectedSessions = listOf(
                    Session("1").apply {
                        subtitle = "New subtitle"
                        changedSubtitle = true
                    }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Speakers differ",
                oldSessions = listOf(Session("1").apply { speakers = listOf("Old speakers") }),
                newSessions = listOf(Session("1").apply { speakers = listOf("New speakers") }),
                expectedSessions = listOf(
                    Session("1").apply {
                        speakers = listOf("New speakers")
                        changedSpeakers = true
                    }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Speakers differ in order",
                oldSessions = listOf(
                    Session("1").apply {
                        speakers = listOf("speaker1", "speaker2", "speaker3")
                    }
                ),
                newSessions = listOf(
                    Session("1").apply {
                        speakers = listOf("speaker3", "speaker1", "speaker2")
                    }
                ),
                expectedSessions = listOf(
                    Session("1").apply {
                        speakers = listOf("speaker3", "speaker1", "speaker2")
                        changedSpeakers = true
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Languages differ",
                oldSessions = listOf(Session("1").apply { language = "Old language" }),
                newSessions = listOf(Session("1").apply { language = "New language" }),
                expectedSessions = listOf(
                    Session("1").apply {
                        language = "New language"
                        changedLanguage = true
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Room names differ",
                oldSessions = listOf(Session("1").apply { roomName = "Old room" }),
                newSessions = listOf(Session("1").apply { roomName = "New room" }),
                expectedSessions = listOf(
                    Session("1").apply {
                        roomName = "New room"
                        changedRoomName = true
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Tracks differ",
                oldSessions = listOf(Session("1").apply { track = "Old track" }),
                newSessions = listOf(Session("1").apply { track = "New track" }),
                expectedSessions = listOf(Session("1").apply {
                    track = "New track"
                    changedTrack = true
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Recording opt-out differs",
                oldSessions = listOf(Session("1").apply { recordingOptOut = false }),
                newSessions = listOf(Session("1").apply { recordingOptOut = true }),
                expectedSessions = listOf(
                    Session("1").apply {
                        recordingOptOut = true
                        changedRecordingOptOut = true
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Day indices differ",
                oldSessions = listOf(Session("1").apply { dayIndex = 1 }),
                newSessions = listOf(Session("1").apply { dayIndex = 2 }),
                expectedSessions = listOf(Session("1").apply {
                    dayIndex = 2
                    changedDayIndex = true
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Start times differ",
                oldSessions = listOf(Session("1").apply { startTime = 100 }),
                newSessions = listOf(Session("1").apply { startTime = 200 }),
                expectedSessions = listOf(Session("1").apply {
                    startTime = 200
                    changedStartTime = true
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Durations differ",
                oldSessions = listOf(Session("1").apply { duration = 45 }),
                newSessions = listOf(Session("1").apply { duration = 60 }),
                expectedSessions = listOf(Session("1").apply {
                    duration = 60
                    changedDuration = true
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Old session is canceled, new session is added",
                oldSessions = listOf(Session("s1")),
                newSessions = listOf(Session("s2")),
                expectedSessions = listOf(
                    Session("s2").apply { changedIsNew = true },
                    Session("s1").apply { changedIsCanceled = true }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
            scenario1Of(
                scenarioDescription = "Multiple properties differ",
                oldSessions = listOf(
                    Session("1").apply {
                        title = "Old title"
                        subtitle = "Old subtitle"
                        speakers = listOf("Old speakers")
                        language = "Old language"
                        roomName = "Old room"
                        dayIndex = 2
                        track = "Old track"
                        recordingOptOut = false
                        startTime = 200
                        duration = 30
                        changedTitle = false
                        changedSubtitle = false
                        changedSpeakers = false
                        changedLanguage = false
                        changedRoomName = false
                        changedDayIndex = false
                        changedTrack = false
                        changedRecordingOptOut = false
                        changedStartTime = false
                        changedDuration = false
                    }
                ),
                newSessions = listOf(
                    Session("1").apply {
                        title = "New title"
                        subtitle = "New subtitle"
                        speakers = listOf("New speakers")
                        language = "New language"
                        roomName = "New room"
                        dayIndex = 3
                        track = "New track"
                        recordingOptOut = true
                        startTime = 300
                        duration = 45
                        changedTitle = false
                        changedSubtitle = false
                        changedSpeakers = false
                        changedLanguage = false
                        changedRoomName = false
                        changedDayIndex = false
                        changedTrack = false
                        changedRecordingOptOut = false
                        changedStartTime = false
                        changedDuration = false
                    }
                ),
                expectedSessions = listOf(
                    Session("1").apply {
                        title = "New title"
                        subtitle = "New subtitle"
                        speakers = listOf("New speakers")
                        language = "New language"
                        roomName = "New room"
                        dayIndex = 3
                        track = "New track"
                        recordingOptOut = true
                        startTime = 300
                        duration = 45
                        changedTitle = true
                        changedSubtitle = true
                        changedSpeakers = true
                        changedLanguage = true
                        changedRoomName = true
                        changedDayIndex = true
                        changedTrack = true
                        changedRecordingOptOut = true
                        changedStartTime = true
                        changedDuration = true
                    }
                ),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = true,
            ),
        )


        @JvmStatic
        fun dataScenarios2() = listOf(
            scenario2Of(
                scenarioDescription = "URLs differ",
                oldSessions = listOf(Session("1").apply { url = "https://www.android.com" }),
                newSessions = listOf(Session("1").apply { url = "https://android.com" }),
                expectedSessions = listOf(Session("1").apply { url = "https://android.com" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Date texts differ",
                oldSessions = listOf(Session("1").apply { dateText = "2023-08-01" }),
                newSessions = listOf(Session("1").apply { dateText = "2023-08-02" }),
                expectedSessions = listOf(Session("1").apply { dateText = "2023-08-02" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "dateUTCs differ",
                oldSessions = listOf(Session("1").apply { dateUTC = 1536332400000L }),
                newSessions = listOf(Session("1").apply { dateUTC = 1536332400001L }),
                expectedSessions = listOf(Session("1").apply { dateUTC = 1536332400001L }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Time zone offsets differ",
                oldSessions = listOf(Session("1").apply {
                    timeZoneOffset = ZoneOffset.of("+02:00")
                }),
                newSessions = listOf(Session("1").apply {
                    timeZoneOffset = ZoneOffset.of("+01:00")
                }),
                expectedSessions = listOf(Session("1").apply {
                    timeZoneOffset = ZoneOffset.of("+01:00")
                }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Relative start times differ",
                oldSessions = listOf(Session("1").apply { relStartTime = 500 }),
                newSessions = listOf(Session("1").apply { relStartTime = 600 }),
                expectedSessions = listOf(Session("1").apply { relStartTime = 600 }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Types differ",
                oldSessions = listOf(Session("1").apply { type = "lecture" }),
                newSessions = listOf(Session("1").apply { type = "workshop" }),
                expectedSessions = listOf(Session("1").apply { type = "workshop" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Slug differs",
                oldSessions = listOf(Session("1").apply { slug = "opening_ceremony" }),
                newSessions = listOf(Session("1").apply { slug = "welcome_ceremony" }),
                expectedSessions = listOf(Session("1").apply { slug = "welcome_ceremony" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Abstracts differ",
                oldSessions = listOf(Session("1").apply { abstractt = "Lorem ipsum" }),
                newSessions = listOf(Session("1").apply { abstractt = "Lorem ipsum dolor" }),
                expectedSessions = listOf(Session("1").apply { abstractt = "Lorem ipsum dolor" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Descriptions differ",
                oldSessions = listOf(Session("1").apply { description = "Lorem ipsum" }),
                newSessions = listOf(Session("1").apply { description = "Lorem ipsum dolor" }),
                expectedSessions = listOf(Session("1").apply { description = "Lorem ipsum dolor" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Links differ",
                oldSessions = listOf(Session("1").apply { links = "https://www.android.com" }),
                newSessions = listOf(Session("1").apply { links = "https://android.com" }),
                expectedSessions = listOf(Session("1").apply { links = "https://android.com" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
            scenario2Of(
                scenarioDescription = "Recording licenses differ",
                oldSessions = listOf(Session("1").apply { recordingLicense = "CC-0" }),
                newSessions = listOf(Session("1").apply { recordingLicense = "CC 0" }),
                expectedSessions = listOf(Session("1").apply { recordingLicense = "CC 0" }),
                expectedOldCanceledSessions = emptyList(),
                expectedFoundNoteworthyChanges = false,
                expectedFoundChanges = true,
            ),
        )

    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("dataScenarios1")
    fun `computeSessionsWithChangeFlags scenarios1`(
        scenarioDescription: String,
        oldSessions: List<Session>,
        newSessions: List<Session>,
        expectedSessions: List<Session>,
        expectedOldCanceledSessions: List<Session>,
        expectedFoundNoteworthyChanges: Boolean,
    ) {
        with(computeSessionsWithChangeFlags(newSessions, oldSessions)) {
            assertThat(sessionsWithChangeFlags).isEqualTo(expectedSessions)
            assertThat(oldCanceledSessions).isEqualTo(expectedOldCanceledSessions)
            assertThat(foundNoteworthyChanges).isEqualTo(expectedFoundNoteworthyChanges)
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("dataScenarios2")
    fun `computeSessionsWithChangeFlags scenarios2`(
        scenarioDescription: String,
        oldSessions: List<Session>,
        newSessions: List<Session>,
        expectedSessions: List<Session>,
        expectedOldCanceledSessions: List<Session>,
        expectedFoundNoteworthyChanges: Boolean,
        expectedFoundChanges: Boolean,
    ) {
        with(computeSessionsWithChangeFlags(newSessions, oldSessions)) {
            assertThat(sessionsWithChangeFlags).isEqualTo(expectedSessions)
            assertThat(oldCanceledSessions).isEqualTo(expectedOldCanceledSessions)
            assertThat(foundNoteworthyChanges).isEqualTo(expectedFoundNoteworthyChanges)
            assertThat(foundChanges).isEqualTo(expectedFoundChanges)
        }
    }

}
