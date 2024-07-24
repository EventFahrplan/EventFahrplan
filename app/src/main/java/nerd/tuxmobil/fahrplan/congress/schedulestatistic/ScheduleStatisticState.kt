package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import info.metadude.android.eventfahrplan.database.models.ColumnStatistic

sealed interface ScheduleStatisticState {
    data object Loading : ScheduleStatisticState
    data class Success(val scheduleStatistic: List<ColumnStatistic>) : ScheduleStatisticState
}
