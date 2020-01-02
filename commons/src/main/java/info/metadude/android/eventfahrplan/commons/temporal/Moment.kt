package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.*

/**
 * An instance represents a moment in time. Most operations are UTC based, and all are immutable.
 * Hence any manipulation creates a new instance.
 * Timezone based dates are retrieved using {#toZonedDateTime}.
 *
 * TODO make field based getters (e.g. getHour) return values relative to UTC.
 *
 * E.g.
 *
 * > Moment().toZonedDateTime(ZoneOffset.of("GMT+1"))
 */
class Moment() {

    private constructor(instant: Instant) : this() {
        time = instant
    }

    constructor(milliseconds: Long) : this() {
        setToMilliseconds(milliseconds)
    }

    /**
     * @param UTCDate must be in ISO-8601 format, e.g. "yyyy-MM-dd", which must be in UTC.
     * */
    constructor(UTCDate: String) : this(LocalDate.parse(UTCDate).atStartOfDay().toInstant(ZoneOffset.UTC))

    private var time: Instant = Instant.now() // UTC
    var zone: ZoneId = ZoneId.systemDefault()
        private set

    val year: Int
        get() = time.atZone(zone).year
    val month: Int
        get() = time.atZone(zone).monthValue
    val monthDay: Int
        get() = time.atZone(zone).dayOfMonth
    val hour: Int
        get() = time.atZone(zone).hour
    val minute: Int
        get() = time.atZone(zone).minute

    fun setToNow() {
        time = Instant.now()
    }

    /**
     * Returns a copy of this moment, reset to 00:00 hours.
     * Example: 2019-12-31 01:30 => 2019-12-31 00:00
     */
    fun startOfDay(): Moment {
        return Moment(toUTCDateTime()
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC))
    }

    fun endOfDay(): Moment {
        return Moment(toUTCDateTime().toLocalDate().atStartOfDay()
                .plusDays(1)
                .minusSeconds(1)
                .toInstant(ZoneOffset.UTC))
    }

    fun setToMilliseconds(milliseconds: Long) {
        time = Instant.ofEpochMilli(milliseconds)
    }

    fun toMilliseconds() = time.toEpochMilli()

    fun toUTCDateTime(): LocalDateTime {
        return time.atZone(ZoneId.of("UTC")).toLocalDateTime()
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
        return false// TODO time.isBefore(moment.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Moment

        if (time != other.time) return false
        if (zone != other.zone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + zone.hashCode()
        return result
    }

}
