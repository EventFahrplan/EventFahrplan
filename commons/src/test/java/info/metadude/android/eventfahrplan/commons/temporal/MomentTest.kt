package info.metadude.android.eventfahrplan.commons.temporal

import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_DAY
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_HOUR
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_SECOND
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.toMoment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class MomentTest {

    private companion object {

        /**
         * Milliseconds representation of 2019-12-30T22:47:57.615Z.
         */
        const val DEC_30_22_47_2019 = 1577746077615

    }

    @Test
    fun `constants return correct values`() {
        assertThat(MILLISECONDS_OF_ONE_SECOND).isEqualTo(1_000)
        assertThat(MILLISECONDS_OF_ONE_MINUTE).isEqualTo(60_000)
        assertThat(MILLISECONDS_OF_ONE_HOUR).isEqualTo(3_600_000L)
        assertThat(MILLISECONDS_OF_ONE_DAY).isEqualTo(86_400_000L)
        assertThat(MINUTES_OF_ONE_DAY).isEqualTo(1_440)
    }

    @Test
    fun `equals returns true for equal moments`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val momentTwo = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(momentOne == momentTwo).isTrue
    }

    @Test
    fun `equals returns false for unequal moments`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val momentTwo = Moment.ofEpochMilli(DEC_30_22_47_2019).plusSeconds(1)

        assertThat(momentOne == momentTwo).isFalse()
    }

    @Test
    fun `equals returns false for null moment`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(momentOne.equals(null)).isFalse()
    }

    @Test
    fun `hashCode returns same value for equal moments`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val momentTwo = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(momentOne.hashCode()).isEqualTo(momentTwo.hashCode())
    }

    @Test
    fun `hashCode returns different values for unequal moments`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val momentTwo = Moment.ofEpochMilli(DEC_30_22_47_2019).plusSeconds(1)

        assertThat(momentOne.hashCode()).isNotEqualTo(momentTwo.hashCode())
    }

    @Test
    fun `compareTo returns the correct integer value when comparing moments`() {
        val momentOne = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val momentTwo = Moment.ofEpochMilli(DEC_30_22_47_2019).plusSeconds(1)

        assertThat(momentOne.compareTo(momentTwo)).isEqualTo(-1)
        assertThat(momentOne.compareTo(momentTwo.minusSeconds(1))).isEqualTo(0)
        assertThat(momentTwo.compareTo(momentOne)).isEqualTo(1)
    }

    @Test
    fun `toString returns toString representation of internal instant`() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(moment.toString()).isEqualTo("2019-12-30T22:47:57.615Z")
    }

    @Test
    fun `date and time properties are correctly mapped`() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(moment.year).isEqualTo(2019)
        assertThat(moment.month).isEqualTo(12)
        assertThat(moment.monthDay).isEqualTo(30)
        assertThat(moment.hour).isEqualTo(22)
        assertThat(moment.minute).isEqualTo(47)
    }

    @Test
    fun `startOfDay sets hour and minute properties to 0`() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val startOfDay = moment.startOfDay()

        assertThat(startOfDay.year).isEqualTo(2019)
        assertThat(startOfDay.month).isEqualTo(12)
        assertThat(startOfDay.monthDay).isEqualTo(30)
        assertThat(startOfDay.hour).isEqualTo(0)
        assertThat(startOfDay.minute).isEqualTo(0)
    }

    @Test
    fun `endOfDay sets hour property to 23 and minute property to 59`() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val endOfDayUTC = moment.endOfDay().toUtcDateTime()

        assertThat(endOfDayUTC.year).isEqualTo(2019)
        assertThat(endOfDayUTC.monthValue).isEqualTo(12)
        assertThat(endOfDayUTC.dayOfMonth).isEqualTo(30)
        assertThat(endOfDayUTC.hour).isEqualTo(23)
        assertThat(endOfDayUTC.minute).isEqualTo(59)
    }

    @Test
    fun `minuteOfDay returns correct minutes value derived from ZonedDateTime`() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 27, 6, 30, 0, 0, ZoneOffset.ofHours(4))
        assertThat(startsAtDate.toMoment().minuteOfDay).isEqualTo((6 - 4) * 60 + 30)
    }

    @Test
    fun `monthDay returns correct day value for leap year day`() {
        // Thursday, February 28, 2019 11:59:59 PM UTC
        assertThat(Moment.ofEpochMilli(1551312000000).monthDay).isEqualTo(28)
    }

    @Test
    fun `monthDay returns correct day value for day after leap year day`() {
        // Friday, March 1, 2019 12:00:00 AM UTC
        assertThat(Moment.ofEpochMilli(1551398400000).monthDay).isEqualTo(1)
    }

    @Test
    fun `startOfDay and toUtcDateTime allow conversion to LocalDate`() {
        val localDateString = "2019-12-30"
        val localDate = LocalDate.parse(localDateString)

        val startOfDay = Moment.ofEpochMilli(DEC_30_22_47_2019).startOfDay().toUtcDateTime().toLocalDate()

        assertThat(startOfDay).isEqualTo(localDate)
    }

    @Test
    fun `toZonedDateTime leaves epoch milliseconds unchanged`() {
        val nowUTC = Moment.now()
        val utcMillis = nowUTC.toMilliseconds()
        val utcSeconds = utcMillis / MILLISECONDS_OF_ONE_SECOND

        val nowTZ = nowUTC.toZonedDateTime(ZoneOffset.ofHours(2))
        val tzSeconds = nowTZ.toEpochSecond()

        assertThat(utcSeconds).isEqualTo(tzSeconds)
    }

    @Test
    fun `parseDate and toUtcDateTime allow conversion to LocalDate`() {
        val localDateString = "2019-12-31"
        val localDate = LocalDate.parse(localDateString)

        val momentLocalDate = Moment.parseDate(localDateString).toUtcDateTime().toLocalDate()

        assertThat(momentLocalDate).isEqualTo(localDate)
    }

    @Test
    fun isBefore() {
        val momentOne = Moment.now()
        val momentTwo = Moment.now().plusSeconds(1)

        assertThat(momentOne.isBefore(momentTwo)).isTrue
        assertThat(momentTwo.isBefore(momentOne)).isFalse
        assertThat(momentOne.isBefore(momentOne)).isFalse
    }

    @Test
    fun isSimultaneousWith() {
        val momentOne = Moment.now()
        val momentTwo = Moment.now().plusSeconds(1)

        assertThat(momentOne.isSimultaneousWith(momentTwo)).isFalse()
        assertThat(momentTwo.isSimultaneousWith(momentOne)).isFalse
        assertThat(momentOne.plusSeconds(1).isSimultaneousWith(momentTwo)).isTrue()
    }

    @Test
    fun isAfter() {
        val momentOne = Moment.now()
        val momentTwo = Moment.now().plusSeconds(1)

        assertThat(momentOne.isAfter(momentTwo)).isFalse
        assertThat(momentTwo.isAfter(momentOne)).isTrue
        assertThat(momentOne.isAfter(momentOne)).isFalse
    }

    @Test
    fun minutesUntil() {
        val momentOne = Moment.now()
        val momentTwo = momentOne.plusMinutes(1)

        assertThat(momentOne.minutesUntil(momentOne)).isEqualTo(0)
        assertThat(momentOne.minutesUntil(momentTwo)).isEqualTo(1)
        assertThat(momentTwo.minutesUntil(momentOne)).isEqualTo(-1)
    }

    @Test
    fun plusMilliseconds() {
        val momentOne = Moment.ofEpochMilli(0).plusMilliseconds(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(1)

        val momentTwo = Moment.ofEpochMilli(1).plusMilliseconds(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun plusSeconds() {
        val momentOne = Moment.ofEpochMilli(0).plusSeconds(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_SECOND.toLong())

        val momentTwo = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_SECOND.toLong()).plusSeconds(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun plusMinutes() {
        val momentOne = Moment.ofEpochMilli(0).plusMinutes(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_MINUTE.toLong())

        val momentTwo = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_MINUTE.toLong()).plusMinutes(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun plusDays() {
        val momentOne = Moment.ofEpochMilli(0).plusDays(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_DAY)

        val momentTwo = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_DAY).plusDays(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun minusHours() {
        val momentOne = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_HOUR).minusHours(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusHours(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_HOUR)
    }

    @Test
    fun minusMinutes() {
        val momentOne = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_MINUTE.toLong()).minusMinutes(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusMinutes(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_MINUTE.toLong())
    }

    @Test
    fun minusSeconds() {
        val momentOne = Moment.ofEpochMilli(MILLISECONDS_OF_ONE_SECOND.toLong()).minusSeconds(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusSeconds(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(MILLISECONDS_OF_ONE_SECOND.toLong())
    }

    @Test
    fun minusMilliseconds() {
        val momentOne = Moment.ofEpochMilli(1).minusMilliseconds(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusMilliseconds(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(1)
    }

}
