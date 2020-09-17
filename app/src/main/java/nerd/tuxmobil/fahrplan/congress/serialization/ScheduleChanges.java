package nerd.tuxmobil.fahrplan.congress.serialization;

import androidx.annotation.NonNull;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.models.Session;

import static kotlin.collections.CollectionsKt.singleOrNull;

public class ScheduleChanges {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasScheduleChanged(@NonNull List<Session> sessions,
                                             @NonNull List<Session> oldSessions) {
        if (oldSessions.isEmpty()) {
            return false;
        }
        boolean changed = false;

        int sessionIndex = oldSessions.size() - 1;
        while (sessionIndex >= 0) {
            Session session = oldSessions.get(sessionIndex);
            if (session.changedIsCanceled) oldSessions.remove(sessionIndex);
            sessionIndex--;
        }

        for (sessionIndex = 0; sessionIndex < sessions.size(); sessionIndex++) {
            Session newSession = sessions.get(sessionIndex);
            Session oldSession = singleOrNull(oldSessions, session -> newSession.sessionId.equals(session.sessionId));

            if (oldSession == null) {
                newSession.changedIsNew = true;
                changed = true;
                continue;
            }

            if (oldSession.equals(newSession)) {
                oldSessions.remove(oldSession);
                continue;
            }

            if (!newSession.title.equals(oldSession.title)) {
                newSession.changedTitle = true;
                changed = true;
            }
            if (!newSession.subtitle.equals(oldSession.subtitle)) {
                newSession.changedSubtitle = true;
                changed = true;
            }
            if (!newSession.speakers.equals(oldSession.speakers)) {
                newSession.changedSpeakers = true;
                changed = true;
            }
            if (!newSession.lang.equals(oldSession.lang)) {
                newSession.changedLanguage = true;
                changed = true;
            }
            if (!newSession.room.equals(oldSession.room)) {
                newSession.changedRoom = true;
                changed = true;
            }
            if (!newSession.track.equals(oldSession.track)) {
                newSession.changedTrack = true;
                changed = true;
            }
            if (newSession.recordingOptOut != oldSession.recordingOptOut) {
                newSession.changedRecordingOptOut = true;
                changed = true;
            }
            if (newSession.day != oldSession.day) {
                newSession.changedDay = true;
                changed = true;
            }
            if (newSession.startTime != oldSession.startTime) {
                newSession.changedTime = true;
                changed = true;
            }
            if (newSession.duration != oldSession.duration) {
                newSession.changedDuration = true;
                changed = true;
            }
            oldSessions.remove(oldSession);
        }

        for (sessionIndex = 0; sessionIndex < oldSessions.size(); sessionIndex++) {
            Session oldSession = oldSessions.get(sessionIndex);
            oldSession.cancel();
            sessions.add(oldSession);
            changed = true;
        }

        return changed;
    }

}
