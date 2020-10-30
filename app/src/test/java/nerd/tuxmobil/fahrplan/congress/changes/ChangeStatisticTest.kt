package nerd.tuxmobil.fahrplan.congress.changes

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test

class ChangeStatisticTest {

    companion object {

        private val unchangedSessions = listOf(
                Session("11111111-1111-1111-1111-111111111111").apply {
                    highlight = false
                    changedTitle = false
                },
                Session("11111111-1111-1111-1111-222222222222").apply {
                    highlight = true
                    changedTitle = false
                })

        private val changedSessions = listOf(
                Session("33333333-3333-3333-3333-333333333333").apply {
                    highlight = true
                    changedTitle = true
                },
                Session("11111111-1111-1111-1111-444444444444").apply {
                    highlight = false
                    changedTitle = true
                })

        private val oldSessions = listOf(
                Session("22222222-2222-2222-2222-111111111111").apply {
                    highlight = false
                    changedIsNew = false
                },
                Session("22222222-2222-2222-2222-222222222222").apply {
                    highlight = true
                    changedIsNew = false
                })

        private val newSessions = listOf(
                Session("2003").apply {
                    highlight = true
                    changedIsNew = true
                },
                Session("2004").apply {
                    highlight = false
                    changedIsNew = true
                })

        private val uncanceledSessions = listOf(
                Session("3001").apply {
                    highlight = false
                    changedIsCanceled = false
                },
                Session("3002").apply {
                    highlight = true
                    changedIsCanceled = false
                })

        private val canceledSessions = listOf(
                Session("3003").apply {
                    highlight = true
                    changedIsCanceled = true
                },
                Session("3004").apply {
                    highlight = false
                    changedIsCanceled = true
                })

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
    private fun ChangeStatistic(sessions: List<Session>) = ChangeStatistic(sessions, NoLogging)

}
