package info.metadude.android.eventfahrplan.commons.temporal

import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.toMoment
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class MomentTest {

    private companion object {

        /**
         * Milliseconds representation of 2019-12-30T22:47:57.615Z.
         */
        private const val DEC_30_22_47_2019 = 1577746077615

    }


    @Test
    fun dateTimeFieldsAreCorrectlyMapped() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)

        assertThat(moment.year).isEqualTo(2019)
        assertThat(moment.month).isEqualTo(12)
        assertThat(moment.monthDay).isEqualTo(30)
        assertThat(moment.hour).isEqualTo(22)
        assertThat(moment.minute).isEqualTo(47)
    }

    @Test
    fun startOfDay() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val startOfDay = moment.startOfDay()

        assertThat(startOfDay.year).isEqualTo(2019)
        assertThat(startOfDay.month).isEqualTo(12)
        assertThat(startOfDay.monthDay).isEqualTo(30)
        assertThat(startOfDay.hour).isEqualTo(0)
        assertThat(startOfDay.minute).isEqualTo(0)
    }

    @Test
    fun endOfDay() {
        val moment = Moment.ofEpochMilli(DEC_30_22_47_2019)
        val endOfDayUTC = moment.endOfDay().toUtcDateTime()

        assertThat(endOfDayUTC.year).isEqualTo(2019)
        assertThat(endOfDayUTC.monthValue).isEqualTo(12)
        assertThat(endOfDayUTC.dayOfMonth).isEqualTo(30)
        assertThat(endOfDayUTC.hour).isEqualTo(23)
        assertThat(endOfDayUTC.minute).isEqualTo(59)
    }

    @Test
    fun getMinuteOfDayZonedInput() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 27, 6, 30, 0, 0, ZoneOffset.ofHours(4))
        assertThat(startsAtDate.toMoment().minuteOfDay).isEqualTo((6 - 4) * 60 + 30)
    }

    @Test
    fun getDayOfMonthWithLeapYearDay() {
        // Thursday, February 28, 2019 11:59:59 PM UTC
        assertThat(Moment.ofEpochMilli(1551312000000).monthDay).isEqualTo(28)
    }

    @Test
    fun getDayOfMonthWithDayAfterLeapYear() {
        // Friday, March 1, 2019 12:00:00 AM UTC
        assertThat(Moment.ofEpochMilli(1551398400000).monthDay).isEqualTo(1)
    }

    @Test
    fun startOfDayVsLocalDate() {
        val localDateString = "2019-12-30"
        val localDate = LocalDate.parse(localDateString)

        val startOfDay = Moment.ofEpochMilli(DEC_30_22_47_2019).startOfDay().toUtcDateTime().toLocalDate()

        assertThat(startOfDay).isEqualTo(localDate)
    }

    @Test
    fun timeZoneHasNoEffectOnMilliseconds() {
        val nowUTC = Moment.now()
        val utcMillis = nowUTC.toMilliseconds()
        val utcSeconds = utcMillis / 1000

        val nowTZ = nowUTC.toZonedDateTime(ZoneOffset.ofHours(2))
        val tzSeconds = nowTZ.toEpochSecond()

        assertThat(utcSeconds).isEqualTo(tzSeconds)
    }

    @Test
    fun toLocalDate() {
        val localDateString = "2019-12-31"
        val localDate = LocalDate.parse(localDateString)

        val momentLocalDate = Moment.parseDate(localDateString).toUtcDateTime().toLocalDate()

        assertThat(momentLocalDate).isEqualTo(localDate)
    }

    @Test
    fun isBefore() {
        val momentOne = Moment.now()
        val momentTwo = Moment.now().plusSeconds(1)

        assertThat(momentOne.isBefore(momentTwo)).isTrue()
        assertThat(momentTwo.isBefore(momentOne)).isFalse()
        assertThat(momentOne.isBefore(momentOne)).isFalse()
    }

    @Test
    fun plusSeconds() {
        val momentOne = Moment.ofEpochMilli(0).plusSeconds(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(1000)

        val momentTwo = Moment.ofEpochMilli(1000).plusSeconds(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun plusMinutes() {
        val momentOne = Moment.ofEpochMilli(0).plusMinutes(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(1000 * 60)

        val momentTwo = Moment.ofEpochMilli(1000 * 60).plusMinutes(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(0)
    }

    @Test
    fun minusHours() {
        val momentOne = Moment.ofEpochMilli(3600 * 1000).minusHours(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusHours(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(3600 * 1000)
    }

    @Test
    fun minusMinutes() {
        val momentOne = Moment.ofEpochMilli(60 * 1000).minusMinutes(1)
        assertThat(momentOne.toMilliseconds()).isEqualTo(0)

        val momentTwo = Moment.ofEpochMilli(0).minusMinutes(-1)
        assertThat(momentTwo.toMilliseconds()).isEqualTo(60 * 1000)
    }
}
