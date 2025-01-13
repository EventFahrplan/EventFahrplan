package nerd.tuxmobil.fahrplan.congress.changes

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class ChangeStatisticTest {

    companion object {

        private val unchangedSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111111001",
                isHighlight = false,
                changedTitle = false,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111111002",
                isHighlight = true,
                changedTitle = false,
            )
        )

        private val changedSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111111003",
                isHighlight = true,
                changedTitle = true,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111111004",
                isHighlight = false,
                changedTitle = true,
            )
        )

        private val oldSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111112001",
                isHighlight = false,
                changedIsNew = false,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111112002",
                isHighlight = true,
                changedIsNew = false,
            )
        )

        private val newSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111112003",
                isHighlight = true,
                changedIsNew = true,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111112004",
                isHighlight = false,
                changedIsNew = true,
            )
        )

        private val uncanceledSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111113001",
                isHighlight = false,
                changedIsCanceled = false,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111113002",
                isHighlight = true,
                changedIsCanceled = false,
            )
        )

        private val canceledSessions = listOf(
            Session(
                guid = "11111111-1111-1111-1111-111111113003",
                isHighlight = true,
                changedIsCanceled = true,
            ),
            Session(
                guid = "11111111-1111-1111-1111-111111113004",
                isHighlight = false,
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
