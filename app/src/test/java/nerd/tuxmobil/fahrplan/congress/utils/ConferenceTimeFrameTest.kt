package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ConferenceTimeFrameTest {

    private companion object {
        // 2015-12-27T00:00:00+0100, in milliseconds: 1451170800000
        const val FIRST_DAY_START_TIME = 1451170800000L

        // 2015-12-31T00:00:00+0100, in milliseconds: 1451516400000
        const val LAST_DAY_END_TIME = 1451516400000L
    }

    private lateinit var conference: ConferenceTimeFrame

    @Before
    fun setUp() {
        conference = ConferenceTimeFrame(FIRST_DAY_START_TIME, LAST_DAY_END_TIME)
    }

    @Test
    fun `isValid return true for first day followed by last day`() {
        assertThat(conference.isValid).isTrue()
    }

    @Test
    fun `isValid throws an exception for last day followed by first day`() {
        try {
            ConferenceTimeFrame(LAST_DAY_END_TIME, FIRST_DAY_START_TIME)
            fail("Expect an IllegalStateException to be thrown.")
        } catch (e: IllegalStateException) {
            assertThat(e.message).startsWith("Invalid conference time frame:")
        }
    }

    @Test
    fun `isValid throws an exception for same day twice`() {
        try {
            ConferenceTimeFrame(FIRST_DAY_START_TIME, FIRST_DAY_START_TIME)
            fail("Expect an IllegalStateException to be thrown.")
        } catch (e: IllegalStateException) {
            assertThat(e.message).startsWith("Invalid conference time frame:")
        }
    }

    @Test
    fun `firstDayStart returns the first day`() {
        assertThat(conference.firstDayStartTime).isEqualTo(FIRST_DAY_START_TIME)
    }

    @Test
    fun `contains returns true if time marks a session at the first day`() {
        // 2015-12-27T11:30:00+0100, in milliseconds: 1451212200000
        assertThat(conference.contains(1451212200000L)).isTrue()
    }

    @Test
    fun `contains returns false if time marks a session starting one second before the first day`() {
        // 2015-12-26T23:59:59+0100, in milliseconds: 1451170799000
        assertThat(conference.contains(1451170799000L)).isFalse()
    }

    @Test
    fun `contains returns false if time marks a session at the last day end time`() {
        assertThat(conference.contains(LAST_DAY_END_TIME)).isFalse()
    }

    @Test
    fun `endsAtOrBefore returns true if time marks a session at the last day end time`() {
        assertThat(conference.endsAtOrBefore(LAST_DAY_END_TIME)).isTrue()
    }

    @Test
    fun `endsAtOrBefore returns false if time marks a session starting one millisecond before the last day end time`() {
        assertThat(conference.endsAtOrBefore(LAST_DAY_END_TIME - 1)).isFalse()
    }

    @Test
    fun `startsAfter returns false if time marks a session at the first day`() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME)).isFalse()
    }

    @Test
    fun `startsAfter returns true if time marks a session starting on millisecond before the first day`() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME - 1)).isTrue()
    }

    @Test
    fun `startsAtOrBefore returns true if time marks a session at the first day`() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME)).isTrue()
    }

    @Test
    fun `startsAtOrBefore returns true if time marks a session starting one millisecond after the first day`() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME + 1)).isTrue()
    }

    @Test
    fun `startsAtOrBefore returns false if time marks a session starting one millisecond before the first day`() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME - 1)).isFalse()
    }

}
