package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import org.junit.jupiter.api.Test

private typealias SessionModification = Session.() -> Unit

class SessionTest {

    companion object {

        fun createSession() = Session("s1").apply {
            title = "Lorem ipsum"
            subtitle = "Gravida arcu ac tortor"
            feedbackUrl = "https://example.com/feedback"
            dayIndex = 3
            dateText = "2020-02-29"
            dateUTC = 1439478900000L
            startTime = 1125
            duration = 60
            roomName = "Main hall"
            roomIdentifier = "88888888-4444-4444-4444-121212121212"
            speakers = listOf("Janet")
            track = "science"
            type = "workshop"
            language = "cz"
            recordingLicense = "CC-0"
            recordingOptOut = true

            // Not considered in equal nor hashCode.
            url = "https://example.com"
            relStartTime = 500
            roomIndex = 6
            slug = "lorem-ipsum"
            abstractt = "Sodales ut etiam sit amet nisl purus"
            description = "Lorem ipsum dolor sit amet"
            links = "http://sample.com"
            highlight = true
            hasAlarm = true

            // Not considered in equal nor hashCode, too.
            changedTitle = true
            changedSubtitle = true
            changedRoomName = true
            changedDayIndex = true
            changedStartTime = true
            changedDuration = true
            changedSpeakers = true
            changedRecordingOptOut = true
            changedLanguage = true
            changedTrack = true
            changedIsNew = true
            changedIsCanceled = true
        }

        fun createSessionModifyingNonConsideredFields() = createSession().apply {
            url = "https://foobar-url.org"
            relStartTime = 999
            roomIndex = 13
            slug = "foo-bar"
            abstractt = "Foo abstract"
            description = "Foo description"
            links = "https://foobar-links.org"
            highlight = false
            hasAlarm = false

            changedTitle = false
            changedSubtitle = false
            changedRoomName = false
            changedDayIndex = false
            changedStartTime = false
            changedDuration = false
            changedSpeakers = false
            changedRecordingOptOut = false
            changedLanguage = false
            changedTrack = false
            changedIsNew = false
            changedIsCanceled = false
        }

    }

    @Test
    fun `copy constructor creates a new instance`() {
        val session1 = createSession()
        val session2 = Session(session1)
        assertThat(session1).isEqualTo(session2)
        assertThat(session1).isNotSameInstanceAs(session2)
    }

    @Test
    fun `equals evaluates true for equal sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1).isEqualTo(session2)
    }

    @Test
    fun `equals evaluates true for sessions with not considered fields modified`() {
        val session1 = createSession()
        val session2 = createSessionModifyingNonConsideredFields()
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1).isEqualTo(session2)
    }

    @Test
    fun `equals evaluates false when comparing with null`() {
        val session1 = createSession()
        val session2 = null
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    @Test
    fun `equals evaluates false when comparing with other type`() {
        val session1 = createSession()
        val session2 = "Other type"
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    @Test
    fun `hashCode evaluates true for equal sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode())
    }

    @Test
    fun `hashCode evaluates true for sessions with not considered fields modified`() {
        val session1 = createSession()
        val session2 = createSessionModifyingNonConsideredFields()
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode())
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd sessionId`() {
        val session2Modification: SessionModification = { sessionId = "Odd session ID" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd title`() {
        val session2Modification: SessionModification = { title = "Odd title" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd subtitle`() {
        val session2Modification: SessionModification = { subtitle = "Odd subtitle" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd feedback URL`() {
        val session2Modification: SessionModification = { feedbackUrl = "https://example.net/feedback" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd day`() {
        val session2Modification: SessionModification = { dayIndex = 2 }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd date`() {
        val session2Modification: SessionModification = { dateText = "1999-12-23" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd dateUTC`() {
        val session2Modification: SessionModification = { dateUTC = 1439471100000L }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd startTime`() {
        val session2Modification: SessionModification = { startTime = 1350 }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd duration`() {
        val session2Modification: SessionModification = { duration = 45 }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd room name`() {
        val session2Modification: SessionModification = { roomName = "Odd room name" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd room identifier`() {
        val session2Modification: SessionModification = { roomIdentifier = "Odd room identifier" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd speakers`() {
        val session2Modification: SessionModification = { speakers = listOf("Odd speakers") }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd track`() {
        val session2Modification: SessionModification = { track = "Odd track" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd type`() {
        val session2Modification: SessionModification = { type = "Odd type" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd lang`() {
        val session2Modification: SessionModification = { language = "Odd language" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd recordingLicense`() {
        val session2Modification: SessionModification = { recordingLicense = "Odd recording license" }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd recordingOptOut`() {
        val session2Modification: SessionModification = { recordingOptOut = false }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    private fun assertOddSessionsAreNotEqual(session2Modification: SessionModification) {
        val session1 = createSession()
        val session2 = createSession().apply { session2Modification() }
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    private fun assertOddSessionsHaveOddHashCodes(session2Modification: SessionModification) {
        val session1 = createSession()
        val session2 = createSession().apply { session2Modification() }
        assertThat(session1).isNotSameInstanceAs(session2)
        assertThat(session1.hashCode()).isNotEqualTo(session2.hashCode())
    }

    @Test
    fun `cancel marks a session as canceled and resets all change other flags`() {
        val session = Session("0").apply {
            changedTitle = true
            changedSubtitle = true
            changedRoomName = true
            changedDayIndex = true
            changedStartTime = true
            changedDuration = true
            changedSpeakers = true
            changedRecordingOptOut = true
            changedLanguage = true
            changedTrack = true
            changedIsNew = true
            changedIsCanceled = false
        }
        val comparableCanceledSession = Session("0").apply {
            changedTitle = false
            changedSubtitle = false
            changedRoomName = false
            changedDayIndex = false
            changedStartTime = false
            changedDuration = false
            changedSpeakers = false
            changedRecordingOptOut = false
            changedLanguage = false
            changedTrack = false
            changedIsNew = false
            changedIsCanceled = true
        }
        val canceledSession = session.cancel()
        assertThat(canceledSession).isEqualTo(comparableCanceledSession)
        assertThat(canceledSession).isNotSameInstanceAs(session)
    }

    @Test
    fun `startsAt returns the start date converted to Moment`() {
        val session = Session("").apply { dateUTC = 1582963200000L }
        assertThat(session.startsAt).isEqualTo(Moment.ofEpochMilli(1582963200000L))
    }

    @Test
    fun `startsAt throws exception if dateUTC is less then or equal to 0`() {
        val session = Session("").apply { dateUTC = 0 }
        try {
            session.startsAt
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Field 'dateUTC' must be more than 0.")
        }
    }

    @Test
    fun `endsAtDateUtc sums dateUTC and duration`() {
        val session = Session("").apply {
            dateUTC = 10_000
            duration = 60
        }
        assertThat(session.endsAt.toMilliseconds()).isEqualTo(3_610_000L)
    }

    @Test
    fun `getEndsAt sums dateUTC and duration`() {
        val session = Session("").apply {
            dateUTC = 1584662400000L
            duration = 120
        }
        val endsAt = Moment.ofEpochMilli(1584662400000L + 120 * MILLISECONDS_OF_ONE_MINUTE)
        assertThat(session.endsAt).isEqualTo(endsAt)
    }

}
