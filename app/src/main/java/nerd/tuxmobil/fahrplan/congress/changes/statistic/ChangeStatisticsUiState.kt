package nerd.tuxmobil.fahrplan.congress.changes.statistic

data class ChangeStatisticsUiState(
    val scheduleVersion: String,
    val statistics: List<ChangeStatisticProperty>,
    val allSessionsCount: Int,
)
