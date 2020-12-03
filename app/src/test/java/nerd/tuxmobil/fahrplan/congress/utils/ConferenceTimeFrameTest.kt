package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ConferenceTimeFrameTest {

    private companion object {
        // 2015-12-27T00:00:00+0100, in seconds: 1451170800000
        const val FIRST_DAY_START_TIME = 1451170800000L

        // 2015-12-31T00:00:00+0100, in seconds: 1451516400000
        const val LAST_DAY_END_TIME = 1451516400000L
    }

    private lateinit var conference: ConferenceTimeFrame

    @Before
    fun setUp() {
        conference = ConferenceTimeFrame(FIRST_DAY_START_TIME, LAST_DAY_END_TIME)
    }

    @Test
    fun isValidWithFirstDayThenLastDay() {
        assertThat(conference.isValid).isTrue()
    }

    @Test
    fun isValidWithLastDayThenFirstDay() {
        try {
            ConferenceTimeFrame(LAST_DAY_END_TIME, FIRST_DAY_START_TIME)
            fail("Expect an IllegalStateException to be thrown.")
        } catch (e: IllegalStateException) {
            assertThat(e.message).startsWith("Invalid conference time frame:")
        }
    }

    @Test
    fun isValidWithSameDayTwice() {
        try {
            ConferenceTimeFrame(FIRST_DAY_START_TIME, FIRST_DAY_START_TIME)
            fail("Expect an IllegalStateException to be thrown.")
        } catch (e: Exception) {
            assertThat(e.message).startsWith("Invalid conference time frame:")
        }
    }

    @Test
    fun firstDayStart() {
        assertThat(conference.firstDayStartTime).isEqualTo(FIRST_DAY_START_TIME)
    }

    @Test
    fun containsWithTimeWithFirstDaySession() {
        // 2015-12-27T11:30:00+0100, in seconds: 1451212200000
        assertThat(conference.contains(1451212200000L)).isTrue()
    }

    @Test
    fun containsWithOneSecondBeforeFirstDay() {
        // 2015-12-26T23:59:59+0100, in seconds: 1451170799000
        assertThat(conference.contains(1451170799000L)).isFalse()
    }

    @Test
    fun containsWithTimeOfLastDay() {
        assertThat(conference.contains(LAST_DAY_END_TIME)).isFalse()
    }

    @Test
    fun endsBeforeWithTimeOfLastDay() {
        assertThat(conference.endsBefore(LAST_DAY_END_TIME)).isTrue()
    }

    @Test
    fun endsBeforeWithTimeBeforeLastDay() {
        assertThat(conference.endsBefore(LAST_DAY_END_TIME - 1)).isFalse()
    }

    @Test
    fun startsAfterWithTimeOfFirstDay() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME)).isFalse()
    }

    @Test
    fun startsAfterWithTimeBeforeFirstDay() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME - 1)).isTrue()
    }

    @Test
    fun startsAtOrBeforeWithTimeOfFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME)).isTrue()
    }

    @Test
    fun startsAtOrBeforeWithTimeAfterFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME + 1)).isTrue()
    }

    @Test
    fun startsAtOrBeforeWithTimeBeforeFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME - 1)).isFalse()
    }

}
