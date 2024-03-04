package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionPropertiesFormatter {

    fun getFormattedSpeakers(session: Session) =
        session.speakers?.joinToString(", ").orEmpty()

    fun getFormattedTrackLanguageText(session: Session) =
        buildString {
            append(session.track)
            if (session.track.isNotEmpty() && session.language.isNotEmpty()) {
                append(" ")
            }
            if (session.language.isNotEmpty()) {
                append("[")
                append(getLanguageText(session))
                append("]")
            }
        }

    fun getLanguageText(session: Session) =
        if (session.language.isEmpty()) {
            ""
        } else {
            session.language
                .replace("-formal", "")
                .replace("German", "de")
                .replace("german", "de")
                .replace("Deutsch", "de")
                .replace("deutsch", "de")
                .replace("English", "en")
                .replace("english", "en")
                .replace("Englisch", "en")
                .replace("englisch", "en")
        }

}
