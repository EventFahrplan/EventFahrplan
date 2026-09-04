package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.models.RoomData

class RenderedAlarmState {

    private val hasAlarmBySessionId = mutableMapOf<String, Boolean>()

    fun clear() = hasAlarmBySessionId.clear()

    fun findNewlyAddedAlarmSessionIds(roomDataList: List<RoomData>) =
        roomDataList
            .asSequence()
            .flatMap { it.sessions }
            .filter { session ->
                val hadNoAlarmWhenLastRendered = hasAlarmBySessionId[session.sessionId] == false
                hadNoAlarmWhenLastRendered && session.hasAlarm
            }
            .map { it.sessionId }
            .toSet()

    fun remember(roomDataList: List<RoomData>) {
        hasAlarmBySessionId.clear()
        roomDataList
            .asSequence()
            .flatMap { it.sessions }
            .forEach { session ->
                hasAlarmBySessionId[session.sessionId] = session.hasAlarm
            }
    }

}
