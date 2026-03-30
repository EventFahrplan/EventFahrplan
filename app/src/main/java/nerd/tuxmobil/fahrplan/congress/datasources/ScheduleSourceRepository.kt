package nerd.tuxmobil.fahrplan.congress.datasources

import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

interface ScheduleSourceRepository {
    fun readScheduleUrl(): String
    fun updateScheduleLastFetchedAt()
    fun updateSessions(sessions: List<SessionNetworkModel>)
    fun updateMeta(meta: MetaDatabaseModel)
}
