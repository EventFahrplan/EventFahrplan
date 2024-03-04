package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class NavigationMenuEntriesGeneratorTest {

    private companion object {
        const val DAY_1_AT_8_AM = 1542528000000 // 2018-11-18T08:00:00Z
        const val DAY_2_AT_230_AM = 1542594600000 // 2018-11-19T02:30:00Z
        const val DAY_2_AT_8_AM = 1542614400000 // 2018-11-19T08:00:00Z
        const val DAY_2_AT_810_AM = 1542615000000 // 2018-11-19T08:10:00Z
        const val DAY_2_AT_830_AM = 1542616200000 // 2018-11-19T08:30:00Z
        const val DAY_3_AT_8_AM = 1542700800000 // 2018-11-20T08:00:00Z
    }

    private val generator = NavigationMenuEntriesGenerator(
        dayString = "Day",
        todayString = "Today",
        logging = NoLogging
    )

    @Test
    fun `getDayMenuEntries returns three day entries with today mark`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-18", startsAt = DAY_1_AT_8_AM, duration = 60),
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 120),
            createSession(dateText = "2018-11-20", startsAt = DAY_3_AT_8_AM, duration = 180),
        )
        val entries = getDayMenuEntries(
            numDays = 3,
            sessions,
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(3)
        assertThat(entries[0]).isEqualTo("Day 1")
        assertThat(entries[1]).isEqualTo("Day 2 - Today")
        assertThat(entries[2]).isEqualTo("Day 3")
    }

    @Test
    fun `getDayMenuEntries returns three day entries although one session happens after midnight`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-18", startsAt = DAY_1_AT_8_AM, duration = 60),
            createSession(dateText = "2018-11-18", startsAt = DAY_2_AT_230_AM, duration = 60),
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 10),
            createSession(dateText = "2018-11-20", startsAt = DAY_3_AT_8_AM, duration = 180),
        )
        val entries = getDayMenuEntries(
            numDays = 3,
            sessions,
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(3)
        assertThat(entries[0]).isEqualTo("Day 1")
        assertThat(entries[1]).isEqualTo("Day 2")
        assertThat(entries[2]).isEqualTo("Day 3")
    }

    @Test
    fun `getDayMenuEntries returns three day entries with today mark expecting sessions for day four`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-18", startsAt = DAY_1_AT_8_AM, duration = 60),
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 120),
            createSession(dateText = "2018-11-20", startsAt = DAY_3_AT_8_AM, duration = 180),
        )
        val entries = getDayMenuEntries(
            numDays = 4,
            sessions,
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(3)
        assertThat(entries[0]).isEqualTo("Day 1")
        assertThat(entries[1]).isEqualTo("Day 2 - Today")
        assertThat(entries[2]).isEqualTo("Day 3")
    }

    @Test
    fun `getDayMenuEntries returns a single day entry without today mark`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 10),
        )
        val entries = getDayMenuEntries(
            numDays = 1,
            sessions,
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(1)
        assertThat(entries.first()).isEqualTo("Day 1")
    }

    @Test
    fun `getDayMenuEntries returns a single day entry with today mark matching the session end`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 10),
        )
        val entries = getDayMenuEntries(
            numDays = 1,
            sessions,
            DAY_2_AT_810_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(1)
        assertThat(entries.first()).isEqualTo("Day 1 - Today")
    }

    @Test
    fun `getDayMenuEntries returns empty day entries when sessions is empty`() {
        val entries = getDayMenuEntries(
            numDays = 1,
            emptyList(),
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(0)
    }

    @Test
    fun `getDayMenuEntries returns empty day entries when number of days is 0`() {
        val entries = getDayMenuEntries(
            numDays = 0,
            emptyList(),
            DAY_2_AT_830_AM,
        )
        assertThat(entries).isNotNull()
        assertThat(entries.size).isEqualTo(0)
    }

    @Test
    fun `getDayMenuEntries throws exception when numDays is less than 0`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-18", startsAt = DAY_1_AT_8_AM, duration = 60),
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 120),
            createSession(dateText = "2018-11-20", startsAt = DAY_3_AT_8_AM, duration = 180),
        )
        try {
            getDayMenuEntries(
                numDays = -1,
                sessions,
                DAY_2_AT_830_AM,
            )
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Number of days is -1 but must be 0 or more.")
        }
    }

    @Test
    fun `getDayMenuEntries throws exception when number of days is less than date list items size`() {
        val sessions = listOf(
            createSession(dateText = "2018-11-18", startsAt = DAY_1_AT_8_AM, duration = 60),
            createSession(dateText = "2018-11-19", startsAt = DAY_2_AT_8_AM, duration = 120),
        )
        try {
            getDayMenuEntries(
                numDays = 1,
                sessions,
                DAY_2_AT_830_AM,
            )
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Expected maximum 1 day(s) but days list contains 2 items.")
        }
    }

    private fun createSession(dateText: String, startsAt: Long, duration: Int) =
        Session("").apply {
            this.dateText = dateText
            this.dateUTC = startsAt
            this.duration = duration
        }

    private fun getDayMenuEntries(numDays: Int, sessions: List<Session>, currentDate: Long) =
        generator.getDayMenuEntries(numDays, sessions, Moment.ofEpochMilli(currentDate))

}
