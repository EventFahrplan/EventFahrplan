package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionPropertiesFormatter(
    private val resourceResolving: ResourceResolving,
) : SessionPropertiesFormatting {

    /**
     * Returns the formatted session [sessionId].
     * An empty string is returned if the [sessionId] is empty itself.
     */
    override fun getFormattedSessionId(sessionId: String) = when (sessionId.isEmpty()) {
        true -> ""
        false -> resourceResolving.getString(R.string.session_details_session_id, sessionId)
    }

    /**
     * Returns the given [links] separated by HTML `br` entities.
     * The original string is returned if no link separator is detected.
     */
    override fun getFormattedLinks(links: String): String {
        // language=regex
        return links.replace("\\),".toRegex(), ")<br>")
    }

    /**
     * Returns the given [url] formatted as an HTML weblink.
     * An empty string is returned if the [url] is empty itself.
     */
    override fun getFormattedUrl(url: String): String {
        return if (url.isEmpty()) "" else "<a href=\"$url\">$url</a>"
    }

    override fun getFormattedSpeakers(session: Session) =
        session.speakers.joinToString(", ")

    override fun getFormattedTrackNameAndLanguageText(session: Session) =
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

    override fun getLanguageText(session: Session) =
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

    override fun getRoomName(
        roomName: String,
        defaultEngelsystemRoomName: String,
        customEngelsystemRoomName: String,
    ) = when (roomName == defaultEngelsystemRoomName) {
        true -> customEngelsystemRoomName
        false -> roomName
    }

}
