package info.metadude.android.eventfahrplan.commons.temporal

import android.text.format.Time

class Moment(

        initializeNow: Boolean = true

) {

    constructor(milliseconds: Long) : this(false) {
        setToMilliseconds(milliseconds)
    }

    private val time = Time()

    init {
        if (initializeNow) {
            time.setToNow()
        }
    }

    var year: Int
        get() = time.year
        set(value) {
            time.year = value
        }

    var month: Int
        get() = time.month
        set(value) {
            time.month = value
        }

    var monthDay: Int
        get() = time.monthDay
        set(value) {
            time.monthDay = value
        }

    var hour: Int
        get() = time.hour
        set(value) {
            time.hour = value
        }

    var minute: Int
        get() = time.minute
        set(value) {
            time.minute = value
        }

    fun setToNow() {
        time.setToNow()
    }

    fun setToMilliseconds(milliseconds: Long) {
        time.set(milliseconds)
    }

    fun toMilliseconds() = time.toMillis(true)

    fun toFormattedString(pattern: String): String = time.format(pattern)

    fun minusHours(hours: Int) {
        time.hour -= hours
    }

    fun minusMinutes(minutes: Int) {
        time.minute -= minutes
    }

    fun plusSeconds(seconds: Int) {
        time.second += seconds
    }

    fun normalize(): Long {
        return time.normalize(true)
    }

    fun isBefore(moment: Moment): Boolean {
        val otherTime = Time()
        otherTime.set(moment.toMilliseconds())
        return time.before(otherTime)
    }

}
