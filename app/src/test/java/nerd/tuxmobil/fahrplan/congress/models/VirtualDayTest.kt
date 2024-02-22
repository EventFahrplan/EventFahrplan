package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.jupiter.api.Test

class VirtualDayTest {

    @Test
    fun `init throws exception if index less then 1`() {
        try {
            VirtualDay(
                index = 0,
                sessions = emptyList()
            )
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Index must be greater than zero.")
        }
    }

    @Test
    fun `init throws exception if sessions are empty`() {
        try {
            VirtualDay(
                index = 1,
                sessions = emptyList()
            )
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Sessions must not be empty.")
        }
    }

    @Test
    fun `timeFrame returns range spanning two natural days`() {
        val virtualDay = VirtualDay(
            index = 1,
            sessions = listOf(
                createSession("2023-12-27", 1703671200000, 60), // 2023-12-27T10:00:00Z
                createSession("2023-12-27", 1703692800000, 30), // 2023-12-27T16:00:00Z
                createSession("2023-12-27", 1703728800000, 45), // 2023-12-28T02:00:00Z
            )
        )
        assertThat(virtualDay.index).isEqualTo(1)
        assertThat(virtualDay.sessions.size).isEqualTo(3)
        val expectedStartsAt = Moment.ofEpochMilli(1703671200000) // 2023-12-27T10:00:00Z
        val expectedEndsAt = Moment.ofEpochMilli(1703731500000) // 2023-12-28T02:54:00Z
        assertThat(virtualDay.timeFrame).isEqualTo(expectedStartsAt..expectedEndsAt)
    }

    @Test
    fun `toString returns index, timeFrame and sessions size`() {
        val virtualDay = VirtualDay(
            index = 4,
            sessions = listOf(
                createSession("2023-12-30", 1703926800000, 90), // 2023-12-30T09:00:00Z
                createSession("2023-12-30", 1703959200000, 30), // 2023-12-30T18:00:00Z
            )
        )
        assertThat("$virtualDay").isEqualTo(
            "VirtualDay(index=4, timeFrame=2023-12-30T09:00:00Z..2023-12-30T18:30:00Z, sessions=2)"
        )
    }

    private fun createSession(dateText: String, startsAt: Long, duration: Int) =
        Session("").apply {
            this.date = dateText
            this.dateUTC = startsAt
            this.duration = duration
        }

}
