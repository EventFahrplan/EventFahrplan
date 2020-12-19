package nerd.tuxmobil.fahrplan.congress.serialization

import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

@Suppress("DataClassPrivateConstructor")
data class ScheduleChanges private constructor(

        val sessionsWithChangeFlags: List<SessionAppModel>,
        val oldCanceledSessions: List<SessionAppModel>,
        val foundChanges: Boolean

) {

    companion object {

        /**
         * Returns a pair of a new list of sessions and a boolean flag indicating whether changes have
         * been found. Each session is flagged as ["new"][SessionAppModel.changedIsNew],
         * ["canceled"][SessionAppModel.changedIsCanceled] or according to the changes detected when
         * comparing it to its equivalent from the [oldSessions] list.
         *
         * This function does not modify the given lists nor any of its elements.
         */
        fun computeSessionsWithChangeFlags(

                newSessions: List<SessionAppModel>,
                oldSessions: List<SessionAppModel>

        ): ScheduleChanges {

            var foundChanges = false
            if (oldSessions.isEmpty()) {
                // Do not flag sessions as "new" when sessions are loaded for the first time.
                return ScheduleChanges(newSessions, emptyList(), foundChanges)
            }

            val oldNotCanceledSessions = oldSessions.filterNot { it.changedIsCanceled }.toMutableList()
            val oldCanceledSessions = oldSessions.filter { it.changedIsCanceled }
            val sessionsWithChangeFlags = mutableListOf<SessionAppModel>()

            var sessionIndex = 0
            while (sessionIndex < newSessions.size) {
                val newSession = newSessions[sessionIndex]
                val oldSession = oldNotCanceledSessions.singleOrNull { oldNotCanceledSession -> newSession.guid == oldNotCanceledSession.guid }
                if (oldSession == null) {
                    sessionsWithChangeFlags += SessionAppModel(newSession).apply { changedIsNew = true }
                    foundChanges = true
                    sessionIndex++
                    continue
                }
                if (oldSession.equalsSession(newSession)) {
                    sessionsWithChangeFlags += newSession
                    oldNotCanceledSessions -= oldSession
                    sessionIndex++
                    continue
                }

                val sessionChange = SessionChange()

                if (newSession.title != oldSession.title) {
                    sessionChange.changedTitle = true
                    foundChanges = true
                }
                if (newSession.subtitle != oldSession.subtitle) {
                    sessionChange.changedSubtitle = true
                    foundChanges = true
                }
                if (newSession.speakers != oldSession.speakers) {
                    sessionChange.changedSpeakers = true
                    foundChanges = true
                }
                if (newSession.lang != oldSession.lang) {
                    sessionChange.changedLanguage = true
                    foundChanges = true
                }
                if (newSession.room != oldSession.room) {
                    sessionChange.changedRoom = true
                    foundChanges = true
                }
                if (newSession.track != oldSession.track) {
                    sessionChange.changedTrack = true
                    foundChanges = true
                }
                if (newSession.recordingOptOut != oldSession.recordingOptOut) {
                    sessionChange.changedRecordingOptOut = true
                    foundChanges = true
                }
                if (newSession.day != oldSession.day) {
                    sessionChange.changedDayIndex = true
                    foundChanges = true
                }
                if (newSession.startTime != oldSession.startTime) {
                    sessionChange.changedStartTime = true
                    foundChanges = true
                }
                if (newSession.duration != oldSession.duration) {
                    sessionChange.changedDuration = true
                    foundChanges = true
                }
                sessionsWithChangeFlags += SessionAppModel(newSession).apply {
                    changedTitle = sessionChange.changedTitle
                    changedSubtitle = sessionChange.changedSubtitle
                    changedSpeakers = sessionChange.changedSpeakers
                    changedLanguage = sessionChange.changedLanguage
                    changedRoom = sessionChange.changedRoom
                    changedTrack = sessionChange.changedTrack
                    changedRecordingOptOut = sessionChange.changedRecordingOptOut
                    changedDay = sessionChange.changedDayIndex
                    changedTime = sessionChange.changedStartTime
                    changedDuration = sessionChange.changedDuration
                }
                oldNotCanceledSessions -= oldSession
                sessionIndex++
            }

            if (oldNotCanceledSessions.isNotEmpty()) {
                // Flag all "old" sessions which are not present in the "new" set as canceled
                // and append them to the "new" set.
                sessionsWithChangeFlags += oldNotCanceledSessions.map { it.toCanceledSession() }
                foundChanges = true
            }

            return ScheduleChanges(sessionsWithChangeFlags.toList(), oldCanceledSessions, foundChanges)
        }

        private data class SessionChange(
                var changedTitle: Boolean = false,
                var changedSubtitle: Boolean = false,
                var changedSpeakers: Boolean = false,
                var changedLanguage: Boolean = false,
                var changedRoom: Boolean = false,
                var changedDayIndex: Boolean = false,
                var changedTrack: Boolean = false,
                var changedRecordingOptOut: Boolean = false,
                var changedStartTime: Boolean = false,
                var changedDuration: Boolean = false
        )

        private fun SessionAppModel.toCanceledSession() = SessionAppModel(this).apply { cancel() }

        private fun SessionAppModel.equalsSession(session: SessionAppModel): Boolean {
            return title == session.title &&
                    subtitle == session.subtitle &&
                    speakers == session.speakers &&
                    lang == session.lang &&
                    room == session.room &&
                    track == session.track &&
                    recordingOptOut == session.recordingOptOut &&
                    day == session.day &&
                    startTime == session.startTime &&
                    duration == session.duration
        }

    }

}
