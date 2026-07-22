package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.FIFTEEN_MINUTES
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TimeSegmentTest {

    private companion object {
        val DAY_20181118_0800 = Moment.ofEpochMilli(1542528000000) // 2018-11-18T08:00:00Z
        val DAY_20181119_0800 = Moment.ofEpochMilli(1542614400000) // 2018-11-19T08:00:00Z
    }

    @Nested
    inner class SameMomentMatching {

        @Test
        fun `isMatched returns false for moment matching time and date because an offset of 0 spans an empty interval`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            assertThat(segment.isMatched(DAY_20181118_0800, minutesOffset = 0)).isFalse()
        }

        @Test
        fun `isMatched returns true for moment matching time and date`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            assertThat(segment.isMatched(DAY_20181118_0800, minutesOffset = FIFTEEN_MINUTES)).isTrue()
        }

    }

    @Nested
    inner class DifferentDayMatching {

        @Test
        fun `isMatched returns false for moment matching time but not day, no offset`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            assertThat(segment.isMatched(DAY_20181119_0800, minutesOffset = 0)).isFalse()
        }

        @Test
        fun `isMatched returns false for moment matching time but not day`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            assertThat(segment.isMatched(DAY_20181119_0800, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

    }

    @Nested
    inner class OffsetBoundary {

        @Test
        fun `isMatched returns false for moment exactly at the offset boundary`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.plusMinutes(15)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

        @Test
        fun `isMatched returns false for moment one minute past the offset boundary`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.plusMinutes(16)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

    }

    @Nested
    inner class MomentBeforeSegment {

        @Test
        fun `isMatched returns false for moment before the segment, no offset`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.minusMinutes(1)
            assertThat(segment.isMatched(moment, minutesOffset = 0)).isFalse()
        }

        @Test
        fun `isMatched returns false for moment before the segment with a non-zero offset`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.minusMinutes(1)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

    }

    @Nested
    inner class DayBoundaryCrossing {

        @Test
        fun `isMatched returns true for moment on the next calendar day within the offset`() {
            val lateNight = DAY_20181118_0800.plusHours(15).plusMinutes(50) // 2018-11-18T23:50:00Z
            val segment = TimeSegment.ofMoment(lateNight)
            val nextDay = lateNight.plusMinutes(14) // 2018-11-19T00:04:00Z
            assertThat(segment.isMatched(nextDay, minutesOffset = FIFTEEN_MINUTES)).isTrue()
        }

        @Test
        fun `isMatched returns false for moment on the next calendar day beyond the offset`() {
            val lateNight = DAY_20181118_0800.plusHours(15).plusMinutes(50) // 2018-11-18T23:50:00Z
            val segment = TimeSegment.ofMoment(lateNight)
            val nextDay = lateNight.plusMinutes(16) // 2018-11-19T00:06:00Z
            assertThat(segment.isMatched(nextDay, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

    }

    @Nested
    inner class SubMinutePrecision {

        @Test
        fun `isMatched returns false for moment before the segment with sub-minute precision`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.minusSeconds(30)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

        @Test
        fun `isMatched returns true for moment within the segment with sub-minute precision`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            val moment = DAY_20181118_0800.plusMinutes(14).plusSeconds(59)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isTrue()
        }

    }

    @Nested
    inner class NegativeOffset {

        @Test
        fun `isMatched throws exception for a negative offset`() {
            val segment = TimeSegment.ofMoment(DAY_20181118_0800)
            try {
                segment.isMatched(DAY_20181118_0800, minutesOffset = -1)
                fail("Expect an IllegalArgumentException to be thrown.")
            } catch (e: IllegalArgumentException) {
                assertThat(e.message).isEqualTo("Minutes offset is -1 but must be 0 or more.")
            }
        }

    }

    @Nested
    inner class RoundedSegmentMoment {

        @Test
        fun `isMatched returns true for unrounded moment within rounded segment`() {
            val moment = DAY_20181118_0800.plusMinutes(3)
            val segment = TimeSegment.ofMoment(moment)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isTrue()
        }

        @Test
        fun `isMatched uses the rounded segment moment, not the original unrounded moment`() {
            val unroundedMoment = DAY_20181118_0800.plusMinutes(3) // 08:03
            val segment = TimeSegment.ofMoment(unroundedMoment) // rounds down to 08:00
            val moment = DAY_20181118_0800.plusMinutes(16)
            assertThat(segment.isMatched(moment, minutesOffset = FIFTEEN_MINUTES)).isFalse()
        }

    }

}
