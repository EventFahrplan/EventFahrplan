package nerd.tuxmobil.fahrplan.congress.changes.statistic

import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.NEW
import nerd.tuxmobil.fahrplan.congress.models.Session

class ChangeStatisticsUiStateFactory(
    private val logging: Logging,
) {

    fun createChangeStatisticsUiState(
        changedSessions: List<Session>,
        allSessionsCount: Int,
        scheduleVersion: String,
    ): ChangeStatisticsUiState {
        val statistic = ChangeStatistic.of(changedSessions, logging)
        val properties = buildList {
            add(ChangeStatisticProperty(statistic.getChangedSessionsCount(), CHANGED))
            add(ChangeStatisticProperty(statistic.getNewSessionsCount(), NEW))
            add(ChangeStatisticProperty(statistic.getCanceledSessionsCount(), CANCELED))
        }
        return ChangeStatisticsUiState(scheduleVersion, properties, allSessionsCount)
    }

}
