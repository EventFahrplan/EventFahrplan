package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.jupiter.api.Test

class NavigationMenuDayTest {

    private companion object {
        val NATURAL_DAY_1_0000 = Moment.ofEpochMilli(1703635200000) // 2023-12-27T00:00:00Z
        val NATURAL_DAY_1_1000 = NATURAL_DAY_1_0000.plusHours(10) // 2023-12-27T10:00:00Z
        val NATURAL_DAY_1_1600 = NATURAL_DAY_1_0000.plusHours(16) // 2023-12-27T16:00:00Z
        val NATURAL_DAY_1_1630 = NATURAL_DAY_1_1600.plusMinutes(30) // 2023-12-27T16:30:00Z
        val NATURAL_DAY_2_0200 = NATURAL_DAY_1_0000.plusDays(1).plusHours(2) // 2023-12-28T02:00:00Z
        val NATURAL_DAY_2_0245 = NATURAL_DAY_2_0200.plusMinutes(45) // 2023-12-28T02:45:00Z
    }

    @Test
    fun `timeFrame spans from the start of the natural day containing the first session till the end of the last session on the same natural day`() {
        val virtualDay = VirtualDay(
            index = 1,
            sessions = listOf(
                createSession("2023-12-27", NATURAL_DAY_1_1000, Duration.ofMinutes(60)),
                createSession("2023-12-27", NATURAL_DAY_1_1600, Duration.ofMinutes(30)),
            ),
        )
        val menuDay = NavigationMenuDay(virtualDay)
        assertThat(menuDay.index).isEqualTo(1)
        assertThat(menuDay.timeFrame).isEqualTo(NATURAL_DAY_1_0000..NATURAL_DAY_1_1630)
    }

    @Test
    fun `timeFrame spans from the start of the natural day containing the first session till the end of the last session on the next natural day`() {
        val virtualDay = VirtualDay(
            index = 1,
            sessions = listOf(
                createSession("2023-12-27", NATURAL_DAY_1_1000, Duration.ofMinutes(60)),
                createSession("2023-12-27", NATURAL_DAY_1_1600, Duration.ofMinutes(30)),
                createSession("2023-12-27", NATURAL_DAY_2_0200, Duration.ofMinutes(45)),
            ),
        )
        val menuDay = NavigationMenuDay(virtualDay)
        assertThat(menuDay.index).isEqualTo(1)
        assertThat(menuDay.timeFrame).isEqualTo(NATURAL_DAY_1_0000..NATURAL_DAY_2_0245)
    }

    private fun createSession(
        @Suppress("SameParameterValue") dateText: String,
        startsAt: Moment,
        duration: Duration,
    ) = Session(
        sessionId = "",
        dateText = dateText,
        dateUTC = startsAt.toMilliseconds(),
        duration = duration,
    )

}
