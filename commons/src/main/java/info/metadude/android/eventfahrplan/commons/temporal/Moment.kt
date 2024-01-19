package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit

/**
 * An instance represents a moment in time. All operations are UTC based.
 * Timezone based dates are retrieved using [Moment.toZonedDateTime].
 *
 * E.g.
 *
 * > Moment.now().toZonedDateTime(ZoneOffset.of("GMT+1"))
 */
class Moment private constructor(private val time: Instant) : Comparable<Moment> {

    val year: Int
        get() = time.atZone(ZoneOffset.UTC).year

    val month: Int
        get() = time.atZone(ZoneOffset.UTC).monthValue

    val monthDay: Int
        get() = time.atZone(ZoneOffset.UTC).dayOfMonth

    val hour: Int
        get() = time.atZone(ZoneOffset.UTC).hour

    val minute: Int
        get() = time.atZone(ZoneOffset.UTC).minute

    val minuteOfDay: Int
        get() = time.atZone(ZoneOffset.UTC).get(ChronoField.MINUTE_OF_DAY)

    /**
     * Returns a copy of this moment, reset to 00:00 hours.
     * Example: 2019-12-31 01:30 => 2019-12-31 00:00
     */
    fun startOfDay(): Moment {
        return Moment(
            toUtcDateTime()
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
        )
    }

    /**
     * Returns a copy of this moment, reset to 23h 59m 59.999s.
     * Example: 2019-12-31 01:30 => 2019-12-31 23:59:59.999
     */
    fun endOfDay(): Moment {
        return Moment(
            toUtcDateTime()
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .toInstant(ZoneOffset.UTC)
        )
    }

    /**
     * Returns this moment represented as milliseconds.
     */
    fun toMilliseconds() = time.toEpochMilli()

    /**
     * Returns this moment as local date normalized to UTC.
     */
    fun toUtcDateTime(): LocalDateTime = time.atZone(ZoneOffset.UTC).toLocalDateTime()

    /**
     * Returns this moment in given [ZoneOffset].
     */
    fun toZonedDateTime(timeZoneOffset: ZoneOffset): ZonedDateTime = time.atZone(timeZoneOffset)

    /**
     * Returns a moment with the given [hours] subtracted.
     */
    fun minusHours(hours: Long): Moment = Moment(time.minus(hours, ChronoUnit.HOURS))

    /**
     * Returns a moment with the given [minutes] subtracted.
     */
    fun minusMinutes(minutes: Long): Moment = Moment(time.minus(minutes, ChronoUnit.MINUTES))

    /**
     * Returns a moment with the given [seconds] subtracted.
     */
    fun minusSeconds(seconds: Long): Moment = Moment(time.minusSeconds(seconds))

    /**
     * Returns a moment with the given [milliseconds] subtracted.
     */
    fun minusMilliseconds(milliseconds: Long): Moment = Moment(time.minusMillis(milliseconds))

    /**
     * Returns a moment with the given [milliseconds] added.
     */
    fun plusMilliseconds(milliseconds: Long): Moment = Moment(time.plusMillis(milliseconds))

    /**
     * Returns a moment with the given [seconds] added.
     */
    fun plusSeconds(seconds: Long): Moment = Moment(time.plusSeconds(seconds))

    /**
     * Returns a moment with the given [minutes] added.
     */
    fun plusMinutes(minutes: Long): Moment = Moment(time.plus(minutes, ChronoUnit.MINUTES))

    /**
     * Returns a moment with the given [days] added.
     */
    fun plusDays(days: Long): Moment = Moment(time.plus(days, ChronoUnit.DAYS))

    /**
     * Returns true if this moment is before the given [moment].
     */
    fun isBefore(moment: Moment): Boolean = time.toEpochMilli() < moment.toMilliseconds()

    /**
     * Returns true if this moment is at the same time as the given [moment].
     */
    fun isSimultaneousWith(moment: Moment): Boolean = time.toEpochMilli() == moment.toMilliseconds()

    /**
     * Returns true if this moment is after the given [moment].
     */
    fun isAfter(moment: Moment): Boolean = moment.isBefore(this)

    /**
     * Returns the duration in minutes between this and the given [moment].
     */
    fun minutesUntil(moment: Moment): Long {
        return Duration.between(time, moment.time).toMinutes()
    }

    override fun compareTo(other: Moment): Int {
        return time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        return time == (other as? Moment)?.time
    }

    override fun hashCode(): Int = time.hashCode()

    override fun toString(): String = time.toString()

    companion object {

        /**
         * 1 minute = 60 seconds
         */
        private const val SECONDS_OF_ONE_MINUTE: Int = 60

        /**
         * 1 second = 1,000 milliseconds
         */
        const val MILLISECONDS_OF_ONE_SECOND: Int = 1000

        /**
         * 1 minute = 60,000 milliseconds
         */
        const val MILLISECONDS_OF_ONE_MINUTE: Int =
            SECONDS_OF_ONE_MINUTE * MILLISECONDS_OF_ONE_SECOND

        /**
         * 1 hour = 3,600,000 milliseconds
         */
        const val MILLISECONDS_OF_ONE_HOUR: Long = 60L * MILLISECONDS_OF_ONE_MINUTE

        /**
         * 1 day = 86,400,000 milliseconds
         */
        const val MILLISECONDS_OF_ONE_DAY: Long = 24L * MILLISECONDS_OF_ONE_HOUR

        /**
         * 1 day = 1,440 minutes
         */
        const val MINUTES_OF_ONE_DAY: Int = 24 * 60

        /**
         * Creates a time zone neutral [Moment] instance of current system clock.
         */
        @JvmStatic
        fun now() = Moment(Instant.now())

        /**
         * Creates a time zone neutral [Moment] instance from given [milliseconds].
         *
         * @param milliseconds epoch millis to create instance from
         */
        @JvmStatic
        fun ofEpochMilli(milliseconds: Long) = Moment(Instant.ofEpochMilli(milliseconds))

        /**
         * Creates a time zone neutral [Moment] instance from given [utcDate].
         *
         * @param utcDate must be in ISO-8601 date format, i.e. "yyyy-MM-dd"
         */
        fun parseDate(utcDate: String) = Moment(
            LocalDate
                .parse(utcDate)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
        )

        /**
         * Creates a time zone neutral [Moment] instance from this [ZonedDateTime].
         */
        fun ZonedDateTime.toMoment() = Moment(this.withZoneSameInstant(ZoneOffset.UTC).toInstant())
    }
}
