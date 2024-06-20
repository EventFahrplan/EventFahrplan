package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.database.models.Session
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper

interface SessionsDatabaseRepository {

    companion object {
        fun get(context: Context, logging: Logging): SessionsDatabaseRepository =
            RealSessionsDatabaseRepository(SessionsDBOpenHelper(context), logging)
    }

    fun insertSessionId(sessionIdContentValues: ContentValues): Int
    fun deleteSessionIdByNotificationId(notificationId: Int): Int

    fun updateSessions(
        contentValuesBySessionId: List<Pair<String, ContentValues>>,
        toBeDeletedSessionIds: List<String>
    )

    fun querySessionBySessionId(sessionId: String): Session
    fun querySessionsForDayIndexOrderedByDateUtc(dayIndex: Int): List<Session>
    fun querySessionsOrderedByDateUtc(): List<Session>
    fun querySessionsWithoutRoom(roomName: String): List<Session>
    fun querySessionsWithinRoom(roomName: String): List<Session>

}
