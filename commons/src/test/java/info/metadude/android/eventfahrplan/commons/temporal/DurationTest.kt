package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Days
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Hours
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Minutes
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Seconds
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_DAY
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_HOUR
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_SECOND
import org.junit.jupiter.api.Test

class DurationTest {

    @Test
    fun `unit returns days`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(duration.unit()).isEqualTo(Days)
    }

    @Test
    fun `unit returns hours`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_HOUR)
        assertThat(duration.unit()).isEqualTo(Hours)
    }

    @Test
    fun `unit returns minutes`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_MINUTE.toLong())
        assertThat(duration.unit()).isEqualTo(Minutes)
    }

    @Test
    fun `unit returns seconds`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_SECOND.toLong())
        assertThat(duration.unit()).isEqualTo(Seconds)
    }

    @Test
    fun `isPositive returns false for negative duration`() {
        val duration = Duration.ofMilliseconds(-1)
        assertThat(duration.isPositive()).isFalse()
    }

    @Test
    fun `isPositive returns false for zero duration`() {
        val duration = Duration.ZERO
        assertThat(duration.isPositive()).isFalse()
    }

    @Test
    fun `isPositive returns true for positive duration`() {
        val duration = Duration.ofMilliseconds(1)
        assertThat(duration.isPositive()).isTrue()
    }

    @Test
    fun `plus returns the sum of two durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_HOUR)
        val sum = durationOne + durationTwo
        assertThat(sum.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_DAY + MILLISECONDS_OF_ONE_HOUR)
    }

    @Test
    fun `minus returns the difference of two durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_HOUR)
        val difference = durationOne - durationTwo
        assertThat(difference.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_DAY - MILLISECONDS_OF_ONE_HOUR)
    }

    @Test
    fun `toWholeDays returns days without decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_DAY * 1.3).toLong())
        assertThat(duration.toWholeDays()).isEqualTo(1)
    }

    @Test
    fun `toWholeHours returns hours without decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_HOUR * 2.5).toLong())
        assertThat(duration.toWholeHours()).isEqualTo(2)
    }

    @Test
    fun `toWholeMinutes returns minutes without decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_MINUTE * 3.8).toLong())
        assertThat(duration.toWholeMinutes()).isEqualTo(3)
    }

    @Test
    fun `toWholeSeconds returns seconds without decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_SECOND * 4.1).toLong())
        assertThat(duration.toWholeSeconds()).isEqualTo(4)
    }

    @Test
    fun `toWholeMilliseconds returns seconds without decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_SECOND * 5.639).toLong())
        assertThat(duration.toWholeMilliseconds()).isEqualTo(5639)
    }

    @Test
    fun `toPartialDays returns days as decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_DAY * 1.3).toLong())
        assertThat(duration.toPartialDays()).isEqualTo(1.3)
    }

    @Test
    fun `toPartialHours returns hours with decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_HOUR * 2.5).toLong())
        assertThat(duration.toPartialHours()).isEqualTo(2.5)
    }

    @Test
    fun `toPartialMinutes returns minutes with decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_MINUTE * 3.8).toLong())
        assertThat(duration.toPartialMinutes()).isEqualTo(3.8)
    }

    @Test
    fun `toPartialSeconds returns seconds with decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_SECOND * 4.1).toLong())
        assertThat(duration.toPartialSeconds()).isEqualTo(4.1)
    }

    @Test
    fun `toPartialMilliseconds returns milliseconds with decimal value`() {
        val duration = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_SECOND * 5.741).toLong())
        assertThat(duration.toPartialMilliseconds()).isEqualTo(5741.0)
    }

    @Test
    fun `equals returns true for equal durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(durationOne == durationTwo).isTrue()
    }

    @Test
    fun `equals returns false for unequal durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY + 1)
        assertThat(durationOne == durationTwo).isFalse()
    }

    @Test
    fun `equals returns false for null duration`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(durationOne.equals(null)).isFalse()
    }

    @Test
    fun `hashCode returns same value for equal durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(durationOne.hashCode()).isEqualTo(durationTwo.hashCode())
    }

    @Test
    fun `hashCode returns different values for unequal durations`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY + 1)
        assertThat(durationOne.hashCode()).isNotEqualTo(durationTwo.hashCode())
    }

    @Test
    fun `compareTo returns the correct integer value when comparing durations (days)`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        val durationTwo = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY + 1)
        val durationThree = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(durationOne.compareTo(durationTwo)).isEqualTo(-1)
        assertThat(durationOne.compareTo(durationThree)).isEqualTo(0)
        assertThat(durationTwo.compareTo(durationOne)).isEqualTo(1)
    }

    @Test
    fun `compareTo returns the correct integer value when comparing durations (seconds)`() {
        val durationOne = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_SECOND.toLong())
        val durationTwo = Duration.ofMilliseconds((MILLISECONDS_OF_ONE_SECOND + 1).toLong())
        val durationThree = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_SECOND.toLong())
        assertThat(durationOne.compareTo(durationTwo)).isEqualTo(-1)
        assertThat(durationOne.compareTo(durationThree)).isEqualTo(0)
        assertThat(durationTwo.compareTo(durationOne)).isEqualTo(1)
    }

    @Test
    fun `toString returns toString representation of internal duration`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(duration.toString()).isEqualTo("PT24H")
    }

    @Test
    fun `ofDays creates Duration from one day`() {
        val duration = Duration.ofDays(1)
        assertThat(duration.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_DAY)
        assertThat(duration.toWholeDays()).isEqualTo(1)
    }

    @Test
    fun `ofHours creates Duration from one hour`() {
        val duration = Duration.ofHours(1)
        assertThat(duration.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_HOUR)
        assertThat(duration.toWholeHours()).isEqualTo(1)
    }

    @Test
    fun `ofMinutes creates Duration from one minute`() {
        val duration = Duration.ofMinutes(1)
        assertThat(duration.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_MINUTE)
        assertThat(duration.toWholeMinutes()).isEqualTo(1)
    }

    @Test
    fun `ofSeconds creates Duration from one second`() {
        val duration = Duration.ofSeconds(1)
        assertThat(duration.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_SECOND)
        assertThat(duration.toWholeSeconds()).isEqualTo(1)
    }

    @Test
    fun `ofMilliseconds creates Duration from milliseconds of one day`() {
        val duration = Duration.ofMilliseconds(MILLISECONDS_OF_ONE_DAY)
        assertThat(duration.toWholeMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_DAY)
    }

    @Test
    fun `ZERO is a Duration of zero days, hours, minutes, milliseconds`() {
        assertThat(Duration.ZERO.toWholeDays()).isEqualTo(0)
        assertThat(Duration.ZERO.toWholeHours()).isEqualTo(0)
        assertThat(Duration.ZERO.toWholeMinutes()).isEqualTo(0)
        assertThat(Duration.ZERO.toWholeSeconds()).isEqualTo(0)
        assertThat(Duration.ZERO.toWholeMilliseconds()).isEqualTo(0)
    }

}
