package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.Clock
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
 * > Moment().toZonedDateTime(ZoneOffset.of("GMT+1"))
 */
class Moment() {

    private var time: Instant = Instant.now(Clock.systemUTC())
    private val utcZoneOffset = ZoneOffset.UTC

    private constructor(instant: Instant) : this() {
        time = instant
    }

    /**
     * Creates a time zone neutral [Moment] instance from given [milliseconds].
     *
     * @param milliseconds epoch millis to create instance from
     */
    constructor(milliseconds: Long) : this() {
        setToMilliseconds(milliseconds)
    }

    /**
     * Creates a time zone neutral [Moment] instance from given [UTCDate].
     *
     * @param UTCDate must be in ISO-8601 date format, e.g. "yyyy-MM-dd"
     */
    constructor(UTCDate: String) : this(LocalDate.parse(UTCDate).atStartOfDay().toInstant(ZoneOffset.UTC))

    /**
     * Creates a time zone neutral [Moment] instance from given [date].
     *
     * @param date any zoned date time to create instance from
     */
    constructor(date: ZonedDateTime) : this(date.withZoneSameInstant(ZoneOffset.UTC).toInstant())

    val year: Int
        get() = time.atZone(utcZoneOffset).year

    val month: Int
        get() = time.atZone(utcZoneOffset).monthValue

    val monthDay: Int
        get() = time.atZone(utcZoneOffset).dayOfMonth

    val hour: Int
        get() = time.atZone(utcZoneOffset).hour

    val minute: Int
        get() = time.atZone(utcZoneOffset).minute

    val minuteOfDay: Int
        get() = time.atZone(utcZoneOffset).get(ChronoField.MINUTE_OF_DAY);

    /**
     * Set this moment instance to current system clock.
     */
    fun setToNow() {
        time = Instant.now(Clock.systemUTC())
    }

    /**
     * Returns a copy of this moment, reset to 00:00 hours.
     * Example: 2019-12-31 01:30 => 2019-12-31 00:00
     */
    fun startOfDay(): Moment {
        return Moment(toUTCDateTime()
                .toLocalDate()
                .atStartOfDay()
                .toInstant(utcZoneOffset))
    }

    /**
     * Returns a copy of this moment, reset to 23h 59m 59.999s.
     * Example: 2019-12-31 01:30 => 2019-12-31 23:59:59.999
     */
    fun endOfDay(): Moment {
        return Moment(toUTCDateTime()
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .toInstant(utcZoneOffset))
    }

    /**
     * Set this moment to given [milliseconds].
     */
    fun setToMilliseconds(milliseconds: Long) {
        time = Instant.ofEpochMilli(milliseconds)
    }

    /**
     * Returns this moment represented as milliseconds.
     */
    fun toMilliseconds() = time.toEpochMilli()

    /**
     * Returns this moment as local date normalized to UTC.
     */
    fun toUTCDateTime(): LocalDateTime = time.atZone(utcZoneOffset).toLocalDateTime()

    /**
     * Returns this moment in given [ZoneOffset].
     */
    fun toZonedDateTime(timeZoneOffset: ZoneOffset): ZonedDateTime = time.atZone(timeZoneOffset)

    /**
     * Subtracts given [hours] from this moment.
     */
    fun minusHours(hours: Long) {
        time = time.minus(hours, ChronoUnit.HOURS)
    }

    /**
     * Subtracts given [minutes] from this moment.
     */
    fun minusMinutes(minutes: Long) {
        time = time.minus(minutes, ChronoUnit.MINUTES)
    }

    /**
     * Adds given [seconds] to this moment.
     */
    fun plusSeconds(seconds: Long) {
        time = time.plusSeconds(seconds)
    }

    /**
     * Adds given [minutes] to this moment.
     */
    fun plusMinutes(minutes: Long) {
        time = time.plus(minutes, ChronoUnit.MINUTES)
    }

    /**
     * Returns true if this moment is before given [moment].
     */
    fun isBefore(moment: Moment): Boolean = time.toEpochMilli() < moment.toMilliseconds()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Moment

        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        return time.hashCode()
    }

}
