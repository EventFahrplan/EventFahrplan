package info.metadude.android.eventfahrplan.commons.temporal

import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Days
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Hours
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Minutes
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Seconds
import org.threeten.bp.Duration as ThreeTenDuration

/**
 * Represents a duration of time.
 */
class Duration private constructor(private val duration: ThreeTenDuration) : Comparable<Duration> {

    sealed interface Unit {
        data object Days : Unit
        data object Hours : Unit
        data object Minutes : Unit
        data object Seconds : Unit
    }

    /**
     * Returns the [Unit] of this duration, from largest to smallest,
     * which fully defines the duration value.
     */
    fun unit() = when {
        toWholeDays() > 0 -> Days
        toWholeHours() > 0 -> Hours
        toWholeMinutes() > 0 -> Minutes
        else -> Seconds
    }

    /**
     * Returns true if this duration is greater than zero.
     */
    fun isPositive() = !duration.isZero && !duration.isNegative

    /**
     * Returns the absolute days of this duration.
     */
    fun toWholeDays() = duration.toDays()

    /**
     * Returns the absolute hours of this duration.
     */
    fun toWholeHours() = duration.toHours()

    /**
     * Returns the absolute minutes of this duration.
     */
    fun toWholeMinutes() = duration.toMinutes()

    /**
     * Returns the absolute seconds of this duration.
     */
    fun toWholeSeconds() = duration.seconds

    /**
     * Returns the absolute milliseconds of this duration.
     */
    fun toWholeMilliseconds() = duration.toMillis()

    /**
     * Returns the precise days of this duration.
     */
    fun toPartialDays() = duration.seconds.toDouble() / (24 * 60 * 60)

    /**
     * Returns the precise hours of this duration.
     */
    fun toPartialHours() = duration.seconds.toDouble() / (60 * 60)

    /**
     * Returns the precise minutes of this duration.
     */
    fun toPartialMinutes() = duration.seconds.toDouble() / 60

    /**
     * Returns the precise seconds of this duration.
     */
    fun toPartialSeconds() = duration.toMillis().toDouble() / 1000

    /**
     * Returns the precise milliseconds of this duration.
     */
    fun toPartialMilliseconds() = duration.toMillis().toDouble()

    override fun compareTo(other: Duration): Int {
        return duration.compareTo(other.duration) / 1_000_000
    }

    override fun equals(other: Any?): Boolean {
        return duration == (other as? Duration)?.duration
    }

    override fun hashCode() = duration.hashCode()

    override fun toString() = duration.toString()

    companion object {

        /**
         * Creates a [Duration] instance from the given [milliseconds].
         *
         * @param milliseconds epoch millis to create instance from
         */
        fun ofMilliseconds(milliseconds: Long) =
            Duration(ThreeTenDuration.ofMillis(milliseconds))

    }

}
