package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoField

/**
 * An instance represents a moment in time. All operations are UTC based.
 * Timezone based dates are retrieved using {#toZonedDateTime}.
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

    constructor(milliseconds: Long) : this() {
        setToMilliseconds(milliseconds)
    }

    /**
     * @param UTCDate must be in ISO-8601 format, e.g. "yyyy-MM-dd"
     * */
    constructor(UTCDate: String) : this(LocalDate.parse(UTCDate).atStartOfDay().toInstant(ZoneOffset.UTC))

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

    fun endOfDay(): Moment {
        return Moment(toUTCDateTime()
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .toInstant(utcZoneOffset))
    }

    fun setToMilliseconds(milliseconds: Long) {
        time = Instant.ofEpochMilli(milliseconds)
    }

    fun toMilliseconds() = time.toEpochMilli()

    fun toUTCDateTime(): LocalDateTime {
        return time.atZone(utcZoneOffset).toLocalDateTime()
    }

    fun toZonedDateTime(timeZoneOffset: ZoneOffset): ZonedDateTime {
        return time.atZone(timeZoneOffset)
    }

    fun minusHours(hours: Int) {
        time = time.minusSeconds((hours * 3600).toLong())
    }

    fun minusMinutes(minutes: Int) {
        time = time.minusSeconds((minutes * 60).toLong())
    }

    fun plusSeconds(seconds: Int) {
        time = time.plusSeconds(seconds.toLong())
    }

    fun isBefore(moment: Moment): Boolean {
        return time.toEpochMilli() < moment.toMilliseconds()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Moment

        if (time != other.time) return false
        if (utcZoneOffset != other.utcZoneOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + utcZoneOffset.hashCode()
        return result
    }

}
