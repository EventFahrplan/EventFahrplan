package nerd.tuxmobil.fahrplan.congress.changes

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

class ChangeStatisticTest {

    companion object {

        private val unchangedLectures = listOf(
                Lecture("1001").apply {
                    highlight = false
                    changedTitle = false
                },
                Lecture("1002").apply {
                    highlight = true
                    changedTitle = false
                })

        private val changedLectures = listOf(
                Lecture("1003").apply {
                    highlight = true
                    changedTitle = true
                },
                Lecture("1004").apply {
                    highlight = false
                    changedTitle = true
                })

        private val oldLectures = listOf(
                Lecture("2001").apply {
                    highlight = false
                    changedIsNew = false
                },
                Lecture("2002").apply {
                    highlight = true
                    changedIsNew = false
                })

        private val newLectures = listOf(
                Lecture("2003").apply {
                    highlight = true
                    changedIsNew = true
                },
                Lecture("2004").apply {
                    highlight = false
                    changedIsNew = true
                })

        private val uncanceledLectures = listOf(
                Lecture("3001").apply {
                    highlight = false
                    changedIsCanceled = false
                },
                Lecture("3002").apply {
                    highlight = true
                    changedIsCanceled = false
                })

        private val canceledLectures = listOf(
                Lecture("3003").apply {
                    highlight = true
                    changedIsCanceled = true
                },
                Lecture("3004").apply {
                    highlight = false
                    changedIsCanceled = true
                })

    }

    @Test
    fun `getChangedLecturesCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getChangedLecturesCount()).isEqualTo(0)
    }

    @Test
    fun `getChangedLecturesCount returns the changed lectures count`() {
        val statistic = ChangeStatistic(unchangedLectures + changedLectures)
        assertThat(statistic.getChangedLecturesCount()).isEqualTo(changedLectures.size)
    }

    @Test
    fun `getNewLecturesCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getNewLecturesCount()).isEqualTo(0)
    }

    @Test
    fun `getNewLecturesCount returns the new lectures count`() {
        val statistic = ChangeStatistic(oldLectures + newLectures)
        assertThat(statistic.getNewLecturesCount()).isEqualTo(newLectures.size)
    }

    @Test
    fun `getCanceledLecturesCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getCanceledLecturesCount()).isEqualTo(0)
    }

    @Test
    fun `getCanceledLecturesCount returns the canceled lectures count`() {
        val statistic = ChangeStatistic(unchangedLectures + canceledLectures)
        assertThat(statistic.getCanceledLecturesCount()).isEqualTo(canceledLectures.size)
    }

    @Test
    fun `getChangedFavoritesCount returns 0 for an empty list`() {
        val statistic = ChangeStatistic(emptyList())
        assertThat(statistic.getChangedFavoritesCount()).isEqualTo(0)
    }

    @Test
    fun `getChangedFavoritesCount returns the changed favorites count`() {
        val statistic = ChangeStatistic(
                unchangedLectures + changedLectures +
                        oldLectures + newLectures +
                        uncanceledLectures + canceledLectures
        )
        assertThat(statistic.getChangedFavoritesCount()).isEqualTo(3)
    }

    @Suppress("TestFunctionName") // Fake constructor to avoid repetition.
    private fun ChangeStatistic(lectures: List<Lecture>) = ChangeStatistic(lectures, NoLogging)

}
