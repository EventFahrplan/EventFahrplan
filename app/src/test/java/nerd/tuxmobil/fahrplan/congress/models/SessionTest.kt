package nerd.tuxmobil.fahrplan.congress.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private typealias SessionModification = Session.() -> Unit

class SessionTest {

    companion object {

        fun createSession() = Session("s1").apply {
            title = "Lorem ipsum"
            subtitle = "Gravida arcu ac tortor"
            day = 3
            date = "2020-02-29"
            dateUTC = 1439478900000L
            startTime = 1125
            duration = 60
            room = "Main hall"
            speakers = listOf("Janet")
            track = "science"
            type = "workshop"
            lang = "cz"
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
            changedRoom = true
            changedDay = true
            changedTime = true
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
            changedRoom = false
            changedDay = false
            changedTime = false
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
        assertThat(session1).isNotSameAs(session2)
    }

    @Test
    fun `equals evaluates true for equal sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1).isEqualTo(session2)
    }

    @Test
    fun `equals evaluates true for sessions with not considered fields modified`() {
        val session1 = createSession()
        val session2 = createSessionModifyingNonConsideredFields()
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1).isEqualTo(session2)
    }

    @Test
    fun `equals evaluates false when comparing with null`() {
        val session1 = createSession()
        val session2 = null
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    @Test
    fun `equals evaluates false when comparing with other type`() {
        val session1 = createSession()
        val session2 = "Other type"
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    @Test
    fun `hashCode evaluates true for equal sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode())
    }

    @Test
    fun `hashCode evaluates true for sessions with not considered fields modified`() {
        val session1 = createSession()
        val session2 = createSessionModifyingNonConsideredFields()
        assertThat(session1).isNotSameAs(session2)
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
    fun `equals evaluates false and hashCode differ for sessions with odd day`() {
        val session2Modification: SessionModification = { day = 2 }
        assertOddSessionsAreNotEqual { session2Modification() }
        assertOddSessionsHaveOddHashCodes { session2Modification() }
    }

    @Test
    fun `equals evaluates false and hashCode differ for sessions with odd date`() {
        val session2Modification: SessionModification = { date = "1999-12-23" }
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
    fun `equals evaluates false and hashCode differ for sessions with odd room`() {
        val session2Modification: SessionModification = { room = "Odd room" }
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
        val session2Modification: SessionModification = { lang = "Odd language" }
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
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1).isNotEqualTo(session2)
    }

    private fun assertOddSessionsHaveOddHashCodes(session2Modification: SessionModification) {
        val session1 = createSession()
        val session2 = createSession().apply { session2Modification() }
        assertThat(session1).isNotSameAs(session2)
        assertThat(session1.hashCode()).isNotEqualTo(session2.hashCode())
    }

    @Test
    fun `cancel marks a session as canceled and resets all change other flags`() {
        val session = Session("0").apply {
            changedTitle = true
            changedSubtitle = true
            changedRoom = true
            changedDay = true
            changedTime = true
            changedDuration = true
            changedSpeakers = true
            changedRecordingOptOut = true
            changedLanguage = true
            changedTrack = true
            changedIsNew = true
            changedIsCanceled = false
        }
        val canceledSession = Session("0").apply {
            changedTitle = false
            changedSubtitle = false
            changedRoom = false
            changedDay = false
            changedTime = false
            changedDuration = false
            changedSpeakers = false
            changedRecordingOptOut = false
            changedLanguage = false
            changedTrack = false
            changedIsNew = false
            changedIsCanceled = true
        }
        assertThat(session.apply { cancel() }).isEqualTo(canceledSession)
    }

    @Test
    fun getStartTimeMoment() {
        val session = Session("1")
        session.relStartTime = 121
        session.date = "2019-12-27"

        val moment = session.startTimeMoment
        assertThat(moment.minute).isEqualTo(1)
        assertThat(moment.minuteOfDay).isEqualTo(121)
        assertThat(moment.hour).isEqualTo(2)
        assertThat(moment.month).isEqualTo(12)
        assertThat(moment.monthDay).isEqualTo(27)
        assertThat(moment.year).isEqualTo(2019)
    }

    @Test
    fun `startTimeMilliseconds returns the dateUTC value when dateUTC is set`() {
        val session = Session("1").apply {
            dateUTC = 1
            date = "2020-03-20"
        }
        assertThat(session.startTimeMilliseconds).isEqualTo(1)
    }

    @Test
    fun `startTimeMilliseconds returns the date value when dateUTC is not set`() {
        val session = Session("1").apply {
            dateUTC = 0
            date = "2020-03-20"
        }
        assertThat(session.startTimeMilliseconds).isEqualTo(1584662400000L)
    }

    @Test
    fun `endsAtTime sums startTime and duration`() {
        val session = Session("").apply {
            startTime = 300
            duration = 30
        }
        assertThat(session.endsAtTime).isEqualTo(330)
    }

    @Test
    fun `endsAtDateUtc sums dateUTC and duration`() {
        val session = Session("").apply {
            dateUTC = 10_000
            duration = 60
        }
        assertThat(session.endsAtDateUtc).isEqualTo(3_610_000L)
    }

}
