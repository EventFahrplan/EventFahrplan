package nerd.tuxmobil.fahrplan.congress.settings

import android.content.Context
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.NextFetch

internal class NextFetchFormatter(
    private val dateFormatter: DateFormatter,
    private val intervalFormatter: IntervalFormatter,
    private val resourceResolver: ResourceResolving,
) {
    fun format(nextFetch: NextFetch): String {
        if (!nextFetch.isValid()) {
            return resourceResolver.getString(R.string.preference_summary_auto_update_enabled)
        }

        val nextFetchAtText = dateFormatter.getFormattedDateTimeShort(
            moment = nextFetch.nextFetchAt,
            sessionZoneOffset = null,
        )
        val intervalText = intervalFormatter.getFormattedInterval(nextFetch.interval)

        return resourceResolver.getString(
            R.string.preference_summary_auto_update_next_fetch_approximately_at,
            nextFetchAtText,
            intervalText
        )
    }

    companion object {
        fun newInstance(context: Context): NextFetchFormatter {
            val resourceResolver = ResourceResolver(context)
            return NextFetchFormatter(
                dateFormatter = DateFormatter.newInstance(useDeviceTimeZone = true),
                intervalFormatter = IntervalFormatter(resourceResolver),
                resourceResolver = resourceResolver,
            )
        }
    }
}
