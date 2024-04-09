package nerd.tuxmobil.fahrplan.congress.changes

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class ChangeStatisticTest {

    companion object {

        private val unchangedSessions = listOf(
            Session(
                sessionId = "1001",
                highlight = false,
                changedTitle = false,
            ),
            Session(
                sessionId = "1002",
                highlight = true,
                changedTitle = false,
            )
        )

        private val changedSessions = listOf(
            Session(
                sessionId = "1003",
                highlight = true,
                changedTitle = true,
            ),
            Session(
                sessionId = "1004",
                highlight = false,
                changedTitle = true,
            )
        )

        private val oldSessions = listOf(
            Session(
                sessionId = "2001",
                highlight = false,
                changedIsNew = false,
            ),
            Session(
                sessionId = "2002",
                highlight = true,
                changedIsNew = false,
            )
        )

        private val newSessions = listOf(
            Session(
                sessionId = "2003",
                highlight = true,
                changedIsNew = true,
            ),
            Session(
                sessionId = "2004",
                highlight = false,
                changedIsNew = true,
            )
        )

        private val uncanceledSessions = listOf(
            Session(
                sessionId = "3001",
                highlight = false,
                changedIsCanceled = false,
            ),
            Session(
                sessionId = "3002",
                highlight = true,
                changedIsCanceled = false,
            )
        )

        private val canceledSessions = listOf(
            Session(
                sessionId = "3003",
                highlight = true,
                changedIsCanceled = true,
            ),
            Session(
                sessionId = "3004",
                highlight = false,
                changedIsCanceled = true,
            )
        )

    }

    @Test
    fun `getChangedSessionsCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getChangedSessionsCount()).isEqualTo(0)
    }

    @Test
    fun `getChangedSessionsCount returns the changed sessions count`() {
        val statistic = ChangeStatistic(unchangedSessions + changedSessions)
        assertThat(statistic.getChangedSessionsCount()).isEqualTo(changedSessions.size)
    }

    @Test
    fun `getNewSessionsCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getNewSessionsCount()).isEqualTo(0)
    }

    @Test
    fun `getNewSessionsCount returns the new sessions count`() {
        val statistic = ChangeStatistic(oldSessions + newSessions)
        assertThat(statistic.getNewSessionsCount()).isEqualTo(newSessions.size)
    }

    @Test
    fun `getCanceledSessionsCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getCanceledSessionsCount()).isEqualTo(0)
    }

    @Test
    fun `getCanceledSessionsCount returns the canceled sessions count`() {
        val statistic = ChangeStatistic(unchangedSessions + canceledSessions)
        assertThat(statistic.getCanceledSessionsCount()).isEqualTo(canceledSessions.size)
    }

    @Test
    fun `getChangedFavoritesCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getChangedFavoritesCount()).isEqualTo(0)
    }

    @Test
    fun `getChangedFavoritesCount returns the changed favorites count`() {
        val statistic = ChangeStatistic(
                unchangedSessions + changedSessions +
                        oldSessions + newSessions +
                        uncanceledSessions + canceledSessions
        )
        assertThat(statistic.getChangedFavoritesCount()).isEqualTo(3)
    }

    @Suppress("TestFunctionName") // Fake constructor to avoid repetition.
    private fun ChangeStatistic(sessions: List<Session>) = ChangeStatistic.of(sessions, NoLogging)

}
