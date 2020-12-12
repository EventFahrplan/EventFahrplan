package nerd.tuxmobil.fahrplan.congress.serialization

import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel2
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsNetworkModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

object ScheduleChanges {

    /**
     * Returns a new list of sessions. Each session is flagged as
     * ["new"][SessionNetworkModel.changedIsNew], ["canceled"][SessionNetworkModel.changedIsCanceled]
     * or according to the changes detected when comparing it to its equivalent from the [oldSessions] list.
     * If any session change is detected then the [onChangesFound] is invoked.
     */
    fun List<SessionNetworkModel>.toFlaggedSessions(oldSessions: List<SessionNetworkModel>, onChangesFound: () -> Unit): List<SessionNetworkModel> {
        var foundChanges = false
        if (oldSessions.isEmpty()) {
            // Do not flag sessions as "new" when sessions are loaded for the first time.
            return this
        }

        val oldNotCanceledSessions = oldSessions.filterNot { it.changedIsCanceled }.toMutableList()
        val flaggedSessions = mutableListOf<SessionNetworkModel>()

        var sessionIndex = 0
        while (sessionIndex < size) {
            val newSession = this[sessionIndex]
            val oldSession = oldNotCanceledSessions.singleOrNull { oldNotCanceledSession -> newSession.sessionId == oldNotCanceledSession.sessionId }
            if (oldSession == null) {
                flaggedSessions += newSession.copy(changedIsNew = true)
                foundChanges = true
                sessionIndex++
                continue
            }
            if (oldSession.equalsSession(newSession)) {
                flaggedSessions += newSession
                oldNotCanceledSessions -= oldSession
                sessionIndex++
                continue
            }
            if (newSession.title != oldSession.title) {
                flaggedSessions += newSession.copy(changedTitle = true)
                foundChanges = true
            }
            if (newSession.subtitle != oldSession.subtitle) {
                flaggedSessions += newSession.copy(changedSubtitle = true)
                foundChanges = true
            }
            if (newSession.speakers != oldSession.speakers) {
                flaggedSessions += newSession.copy(changedSpeakers = true)
                foundChanges = true
            }
            if (newSession.language != oldSession.language) {
                flaggedSessions += newSession.copy(changedLanguage = true)
                foundChanges = true
            }
            if (newSession.room != oldSession.room) {
                flaggedSessions += newSession.copy(changedRoom = true)
                foundChanges = true
            }
            if (newSession.track != oldSession.track) {
                flaggedSessions += newSession.copy(changedTrack = true)
                foundChanges = true
            }
            if (newSession.recordingOptOut != oldSession.recordingOptOut) {
                flaggedSessions += newSession.copy(changedRecordingOptOut = true)
                foundChanges = true
            }
            if (newSession.dayIndex != oldSession.dayIndex) {
                flaggedSessions += newSession.copy(changedDayIndex = true)
                foundChanges = true
            }
            if (newSession.startTime != oldSession.startTime) {
                flaggedSessions += newSession.copy(changedStartTime = true)
                foundChanges = true
            }
            if (newSession.duration != oldSession.duration) {
                flaggedSessions += newSession.copy(changedDuration = true)
                foundChanges = true
            }
            oldNotCanceledSessions -= oldSession
            sessionIndex++
        }

        if (oldNotCanceledSessions.isNotEmpty()) {
            // Flag all "old" sessions which are not present in the "new" set as canceled
            // and append them to the "new" set.
            flaggedSessions += oldNotCanceledSessions.map { it.toCanceledSession() }
            foundChanges = true
        }

        return flaggedSessions.toList().also { if (foundChanges) onChangesFound() }
    }

    private fun SessionNetworkModel.toCanceledSession() = copy(
            changedIsCanceled = true,
            changedIsNew = false,
            changedTitle = false,
            changedSubtitle = false,
            changedRoom = false,
            changedDayIndex = false,
            changedSpeakers = false,
            changedRecordingOptOut = false,
            changedLanguage = false,
            changedTrack = false,
            changedStartTime = false,
            changedDuration = false
    )

    private fun SessionNetworkModel.equalsSession(session: SessionNetworkModel): Boolean {
        return title == session.title &&
                subtitle == session.subtitle &&
                speakers == session.speakers &&
                language == session.language &&
                room == session.room &&
                track == session.track &&
                recordingOptOut == session.recordingOptOut &&
                dayIndex == session.dayIndex &&
                startTime == session.startTime &&
                duration == session.duration
    }

    @Deprecated(message = "Use toFlaggedSessions instead which does not alter the original sessions list.")
    fun hasScheduleChanged(sessions: MutableList<SessionAppModel>, oldSessions: MutableList<SessionAppModel>): Boolean {
        var hasChanged = false
        val flaggedSessions = sessions.toSessionsNetworkModel().toFlaggedSessions(oldSessions.toSessionsNetworkModel()) {
            hasChanged = true
        }
        sessions.clear()
        sessions += flaggedSessions.toSessionsAppModel2()
        return hasChanged
    }
}