package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.*

// TODO Moment should be UTC and initialized with now per default
class Moment() {

    private constructor(instant: Instant) : this() {
        time = instant
    }

    constructor(milliseconds: Long) : this() {
        setToMilliseconds(milliseconds)
    }

    /**
     * @param localDate must be in ISO-8601 format, e.g. "YYYY-MM-DD", which is always UTC.
     * */
    constructor(localDate: String) : this(Instant.parse(localDate).toEpochMilli())

    private var time: Instant = Instant.now() // UTC
    private val zone: ZoneId = ZoneId.systemDefault()
    private val millisPerDay = 86400000

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
        return Moment(LocalDate
                .ofEpochDay(time.toEpochMilli() / millisPerDay)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC))
    }

    fun setToMilliseconds(milliseconds: Long) {
        time = Instant.ofEpochMilli(milliseconds)
    }

    fun toMilliseconds() = time.toEpochMilli()

    fun toLocalDateTime(): LocalDateTime {
        return time.atZone(ZoneId.of("UTC")).toLocalDateTime()
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
