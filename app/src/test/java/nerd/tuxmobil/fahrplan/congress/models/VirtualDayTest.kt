package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.jupiter.api.Test

class VirtualDayTest {

    private companion object {
        val NATURAL_DAY_1_1000 = Moment.ofEpochMilli(1703671200000) // 2023-12-27T10:00:00Z
        val NATURAL_DAY_1_1600 = NATURAL_DAY_1_1000.plusHours(6) // 2023-12-27T16:00:00Z
        val NATURAL_DAY_2_0200 = Moment.ofEpochMilli(1703728800000) // 2023-12-28T02:00:00Z
        val NATURAL_DAY_2_0245 = NATURAL_DAY_2_0200.plusMinutes(45) // 2023-12-28T02:45:00Z
        val NATURAL_DAY_4_0900 = Moment.ofEpochMilli(1703926800000) // 2023-12-30T09:00:00Z
        val NATURAL_DAY_4_1800 = NATURAL_DAY_4_0900.plusHours(9) // 2023-12-30T18:00:00Z
    }

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
                createSession("2023-12-27", NATURAL_DAY_1_1000, Duration.ofMinutes(60)),
                createSession("2023-12-27", NATURAL_DAY_1_1600, Duration.ofMinutes(30)),
                createSession("2023-12-27", NATURAL_DAY_2_0200, Duration.ofMinutes(45)),
            )
        )
        assertThat(virtualDay.index).isEqualTo(1)
        assertThat(virtualDay.sessions.size).isEqualTo(3)
        assertThat(virtualDay.timeFrame).isEqualTo(NATURAL_DAY_1_1000..NATURAL_DAY_2_0245)
    }

    @Test
    fun `toString returns index, timeFrame and sessions size`() {
        val virtualDay = VirtualDay(
            index = 4,
            sessions = listOf(
                createSession("2023-12-30", NATURAL_DAY_4_0900, Duration.ofMinutes(90)),
                createSession("2023-12-30", NATURAL_DAY_4_1800, Duration.ofMinutes(30)),
            )
        )
        assertThat("$virtualDay").isEqualTo(
            "VirtualDay(index=4, timeFrame=2023-12-30T09:00:00Z..2023-12-30T18:30:00Z, sessions=2)"
        )
    }

    private fun createSession(dateText: String, startsAt: Moment, duration: Duration) =
        Session(
            sessionId = "",
            dateText = dateText,
            dateUTC = startsAt.toMilliseconds(),
            duration = duration,
        )

}
