package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.models.VirtualDay
import org.junit.jupiter.api.Test

/**
 * Covers [SessionsExtensions.toVirtualDays][toVirtualDays].
 */
class SessionsExtensionsToVirtualDaysTest {

    @Test
    fun `toVirtualDays returns empty list of days`() {
        val virtualDays = emptyList<Session>().toVirtualDays()
        assertThat(virtualDays).isEmpty()
    }

    @Test
    fun `toVirtualDays returns list of days with sessions broken into virtual days`() {
        val sessions = listOf(
            createSession("2023-12-27", dayIndex = 1, 1703671200000, Duration.ofMinutes(60)), // 2023-12-27T10:00:00Z
            createSession("2023-12-27", dayIndex = 1, 1703728800000, Duration.ofMinutes(45)), // 2023-12-28T02:00:00Z
            createSession("2023-12-28", dayIndex = 2, 1703757600000, Duration.ofMinutes(60)), // 2023-12-28T10:00:00Z
            createSession("2023-12-29", dayIndex = 3, 1703844000000, Duration.ofMinutes(60)), // 2023-12-29T10:00:00Z
            createSession("2023-12-30", dayIndex = 4, 1703930400000, Duration.ofMinutes(60)), // 2023-12-30T10:00:00Z
        )
        val virtualDays = sessions.toVirtualDays()
        val expectedVirtualDays = listOf(
            VirtualDay(1, listOf(sessions[0], sessions[1])),
            VirtualDay(2, listOf(sessions[2])),
            VirtualDay(3, listOf(sessions[3])),
            VirtualDay(4, listOf(sessions[4])),
        )
        assertThat(virtualDays.size).isEqualTo(4)
        assertThat(virtualDays).isEqualTo(expectedVirtualDays)
    }

    @Test
    fun `toVirtualDays returns list of days with correct day indices even if sessions of day 1 are missing`() {
        val sessions = listOf(
            createSession("2023-12-28", dayIndex = 2, 1703757600000, Duration.ofMinutes(60)), // 2023-12-28T10:00:00Z
        )
        val virtualDays = sessions.toVirtualDays()
        val expectedVirtualDays = listOf(
            VirtualDay(2, listOf(sessions[0])),
        )
        assertThat(virtualDays.size).isEqualTo(1)
        assertThat(virtualDays).isEqualTo(expectedVirtualDays)
    }

    private fun createSession(
        dateText: String,
        dayIndex: Int,
        startsAt: Long,
        duration: Duration,
    ) = Session(
        sessionId = "",
        dateText = dateText,
        dayIndex = dayIndex,
        dateUTC = startsAt,
        duration = duration,
    )

}
