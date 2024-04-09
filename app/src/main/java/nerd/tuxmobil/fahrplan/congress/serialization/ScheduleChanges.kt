package nerd.tuxmobil.fahrplan.congress.serialization

import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

@Suppress("DataClassPrivateConstructor")
data class ScheduleChanges private constructor(

        val sessionsWithChangeFlags: List<SessionAppModel>,
        val oldCanceledSessions: List<SessionAppModel>,
        val foundNoteworthyChanges: Boolean,
        val foundChanges: Boolean,

) {

    companion object {

        /**
         * Returns a pair of a new list of sessions and two boolean flags indicating whether changes
         * have been found. [foundNoteworthyChanges] indicates generic changes which are relevant
         * for the schedule changes screen. [foundChanges] is based on the comparison of all
         * session properties. Further, each session is flagged as ["new"][SessionAppModel.changedIsNew],
         * ["canceled"][SessionAppModel.changedIsCanceled] or according to the changes detected when
         * comparing it to its equivalent from the [oldSessions] list.
         *
         * This function does not modify the given lists nor any of its elements.
         */
        fun computeSessionsWithChangeFlags(

                newSessions: List<SessionAppModel>,
                oldSessions: List<SessionAppModel>

        ): ScheduleChanges {

            var foundNoteworthyChanges = false
            var foundChanges = false
            if (oldSessions.isEmpty()) {
                // Do not flag sessions as "new" when sessions are loaded for the first time.
                return ScheduleChanges(newSessions, emptyList(), foundNoteworthyChanges = false, foundChanges = false)
            }

            val oldNotCanceledSessions = oldSessions.filterNot { it.changedIsCanceled }.toMutableList()
            val oldCanceledSessions = oldSessions.filter { it.changedIsCanceled }
            val sessionsWithChangeFlags = mutableListOf<SessionAppModel>()

            var sessionIndex = 0
            while (sessionIndex < newSessions.size) {
                val newSession = newSessions[sessionIndex]
                val oldSession = oldNotCanceledSessions.singleOrNull { oldNotCanceledSession -> newSession.sessionId == oldNotCanceledSession.sessionId }
                if (oldSession == null) {
                    sessionsWithChangeFlags += newSession.copy(changedIsNew = true)
                    foundNoteworthyChanges = true
                    foundChanges = true
                    sessionIndex++
                    continue
                }

                if (!foundChanges && !oldSession.equalsContentWise(newSession)) {
                    foundChanges = true
                }

                if (oldSession.equalsInNoteworthyProperties(newSession)) {
                    sessionsWithChangeFlags += newSession
                    oldNotCanceledSessions -= oldSession
                    sessionIndex++
                    continue
                }

                val sessionChange = SessionChange()

                if (newSession.title != oldSession.title) {
                    sessionChange.changedTitle = true
                    foundNoteworthyChanges = true
                }
                if (newSession.subtitle != oldSession.subtitle) {
                    sessionChange.changedSubtitle = true
                    foundNoteworthyChanges = true
                }
                if (newSession.speakers != oldSession.speakers) {
                    sessionChange.changedSpeakers = true
                    foundNoteworthyChanges = true
                }
                if (newSession.language != oldSession.language) {
                    sessionChange.changedLanguage = true
                    foundNoteworthyChanges = true
                }
                if (newSession.roomName != oldSession.roomName) {
                    sessionChange.changedRoom = true
                    foundNoteworthyChanges = true
                }
                if (newSession.track != oldSession.track) {
                    sessionChange.changedTrack = true
                    foundNoteworthyChanges = true
                }
                if (newSession.recordingOptOut != oldSession.recordingOptOut) {
                    sessionChange.changedRecordingOptOut = true
                    foundNoteworthyChanges = true
                }
                if (newSession.dayIndex != oldSession.dayIndex) {
                    sessionChange.changedDayIndex = true
                    foundNoteworthyChanges = true
                }
                if (newSession.startTime != oldSession.startTime) {
                    sessionChange.changedStartTime = true
                    foundNoteworthyChanges = true
                }
                if (newSession.duration != oldSession.duration) {
                    sessionChange.changedDuration = true
                    foundNoteworthyChanges = true
                }
                sessionsWithChangeFlags += newSession.copy(
                    changedTitle = sessionChange.changedTitle,
                    changedSubtitle = sessionChange.changedSubtitle,
                    changedSpeakers = sessionChange.changedSpeakers,
                    changedLanguage = sessionChange.changedLanguage,
                    changedRoomName = sessionChange.changedRoom,
                    changedTrack = sessionChange.changedTrack,
                    changedRecordingOptOut = sessionChange.changedRecordingOptOut,
                    changedDayIndex = sessionChange.changedDayIndex,
                    changedStartTime = sessionChange.changedStartTime,
                    changedDuration = sessionChange.changedDuration,
                )
                oldNotCanceledSessions -= oldSession
                sessionIndex++
            }

            if (oldNotCanceledSessions.isNotEmpty()) {
                // Flag all "old" sessions which are not present in the "new" set as canceled
                // and append them to the "new" set.
                sessionsWithChangeFlags += oldNotCanceledSessions.map { it.cancel() }
                foundNoteworthyChanges = true
            }

            return ScheduleChanges(
                sessionsWithChangeFlags = sessionsWithChangeFlags.toList(),
                oldCanceledSessions = oldCanceledSessions,
                foundNoteworthyChanges = foundNoteworthyChanges,
                foundChanges = foundChanges,
            )
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

        private fun SessionAppModel.equalsInNoteworthyProperties(session: SessionAppModel): Boolean {
            return title == session.title &&
                    subtitle == session.subtitle &&
                    speakers == session.speakers &&
                    language == session.language &&
                    roomName == session.roomName &&
                    track == session.track &&
                    recordingOptOut == session.recordingOptOut &&
                    dayIndex == session.dayIndex &&
                    startTime == session.startTime &&
                    duration == session.duration
        }

        /**
         * Intentionally omit volatile properties such as [SessionAppModel.hasAlarm],
         * [SessionAppModel.highlight] which are only relevant for the UI layer.
         * Also omit change flags such as [SessionAppModel.changedIsNew].
         *
         * Once [SessionAppModel] is converted into a Kotlin data class and its properties
         * are separated this function can be replaced by an equals comparison.
         */
        private fun SessionAppModel.equalsContentWise(session: SessionAppModel): Boolean {
            return equalsInNoteworthyProperties(session) &&
                    url == session.url &&
                    dateText == session.dateText &&
                    dateUTC == session.dateUTC &&
                    timeZoneOffset == session.timeZoneOffset &&
                    relStartTime == session.relStartTime &&
                    type == session.type &&
                    slug == session.slug &&
                    abstractt == session.abstractt &&
                    description == session.description &&
                    links == session.links &&
                    recordingLicense == session.recordingLicense
        }

    }

}
