package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Test

class NavigationMenuEntriesGeneratorTest {

    private val generator = NavigationMenuEntriesGenerator(
        dayString = "Day",
        todayString = "Today",
        logging = NoLogging
    )

    @Test
    fun `getDayMenuEntries returns three day entries with today mark`() {
        val dateInfoList = DateInfos()
        dateInfoList.add(DateInfo(1, Moment.parseDate("2018-11-18")))
        dateInfoList.add(DateInfo(2, Moment.parseDate("2018-11-19")))
        dateInfoList.add(DateInfo(3, Moment.parseDate("2018-11-20")))
        val entries = getDayMenuEntries(3, dateInfoList, "2018-11-19")
        assertThat(entries).isNotNull
        assertThat(entries.size).isEqualTo(3)
        assertThat(entries[0]).isEqualTo("Day 1")
        assertThat(entries[1]).isEqualTo("Day 2 - Today")
        assertThat(entries[2]).isEqualTo("Day 3")
    }

    @Test
    fun `getDayMenuEntries fails when date info list is empty`() {
        try {
            getDayMenuEntries(1, DateInfos(), "2018-11-19")
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Invalid date info list: []")
        }
    }

    @Test
    fun `getDayMenuEntries fails when date info list is null`() {
        try {
            getDayMenuEntries(1, null, "2018-11-19")
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Invalid date info list: null")
        }
    }

    @Test
    fun `getDayMenuEntries returns a single day entry`() {
        val dateInfoList = DateInfos()
        dateInfoList.add(DateInfo(1, Moment.parseDate("2018-11-18")))
        val entries = getDayMenuEntries(1, dateInfoList, "2018-11-19")
        assertThat(entries).isNotNull
        assertThat(entries.size).isEqualTo(1)
        assertThat(entries[0]).isEqualTo("Day 1")
    }

    @Test
    fun `getDayMenuEntries fails when date info list lacks current date`() {
        val dateInfoList = DateInfos()
        dateInfoList.add(DateInfo(1, Moment.parseDate("2018-11-18")))
        try {
            getDayMenuEntries(0, dateInfoList, "2018-11-19")
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Invalid number of days: 0")
        }
    }

    @Test
    fun `getDayMenuEntries fails when number of days is negative`() {
        val dateInfoList = DateInfos()
        dateInfoList.add(DateInfo(1, Moment.parseDate("2018-11-18")))
        try {
            getDayMenuEntries(-1, dateInfoList, "2018-11-19")
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Invalid number of days: -1")
        }
    }

    private fun getDayMenuEntries(numDays: Int, dateInfos: DateInfos?, currentDate: String) =
        generator.getDayMenuEntries(numDays, dateInfos, Moment.parseDate(currentDate))

}
