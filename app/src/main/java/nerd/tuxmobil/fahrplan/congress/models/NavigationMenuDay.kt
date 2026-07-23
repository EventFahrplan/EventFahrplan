package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment

/**
 * Represents the [time frame][timeFrame] used to determine whether a [virtual day][VirtualDay] is
 * marked as "today" in the navigation menu. The time frame starts at the natural day containing
 * the first session and ends at the end of the last session.
 */
@ConsistentCopyVisibility
data class NavigationMenuDay private constructor(
    val index: Int,
    val timeFrame: ClosedRange<Moment>,
) {

    constructor(virtualDay: VirtualDay) : this(
        index = virtualDay.index,
        timeFrame = virtualDay.timeFrame.start.startOfDay()..virtualDay.timeFrame.endInclusive,
    )

}
