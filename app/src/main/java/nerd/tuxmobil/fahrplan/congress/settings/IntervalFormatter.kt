package nerd.tuxmobil.fahrplan.congress.settings

import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Days
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Hours
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Minutes
import info.metadude.android.eventfahrplan.commons.temporal.Duration.Unit.Seconds
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import kotlin.math.ceil

class IntervalFormatter(private val resolving: ResourceResolving) {

    fun getFormattedInterval(interval: Duration) = when (interval.unit()) {
        Days -> {
            val days = interval.toPartialDays()
            val quantity = quantityOf(days)
            val value = formattedValueOf(days)
            resolving.getQuantityString(R.plurals.preference_summary_auto_update_interval_days, quantity, value)
        }

        Hours -> {
            val hours = interval.toPartialHours()
            val quantity = quantityOf(hours)
            val value = formattedValueOf(hours)
            resolving.getQuantityString(R.plurals.preference_summary_auto_update_interval_hours, quantity, value)
        }

        Minutes -> {
            val minutes = interval.toPartialMinutes()
            val quantity = quantityOf(minutes)
            val value = formattedValueOf(minutes)
            resolving.getQuantityString(R.plurals.preference_summary_auto_update_interval_minutes, quantity, value)
        }

        Seconds -> {
            val seconds = interval.toPartialSeconds()
            val quantity = quantityOf(seconds)
            val value = formattedValueOf(seconds)
            resolving.getQuantityString(R.plurals.preference_summary_auto_update_interval_seconds, quantity, value)
        }
    }

    private fun quantityOf(duration: Double) = ceil(duration).toInt()

    private fun formattedValueOf(duration: Double) = when (duration % 1.0 == 0.0) {
        true -> "${duration.toInt()}"
        false -> "%.1f".format(duration)
    }

}
