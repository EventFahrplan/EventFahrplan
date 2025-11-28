package nerd.tuxmobil.fahrplan.congress.changes.statistic

import nerd.tuxmobil.fahrplan.congress.changes.ChangeType

data class ChangeStatisticProperty(
    val value: Int,
    val changeType: ChangeType,
)
