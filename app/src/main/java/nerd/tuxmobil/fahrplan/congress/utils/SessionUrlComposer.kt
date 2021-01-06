package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PENTABARF

class SessionUrlComposer @JvmOverloads constructor(

        private val session: Session,
        private val sessionUrlTemplate: String = BuildConfig.EVENT_URL,
        private val serverBackEndType: String = BuildConfig.SERVER_BACKEND_TYPE,
        private val specialRoomNames: Set<String> = setOf(
                AppRepository.ENGELSYSTEM_ROOM_NAME,
                "ChaosTrawler", // rc3 2020
                "rC3 Lounge", // rc3 2020
        )

) {

    /**
     * Returns the website URL for the [session] if it can be composed
     * otherwise an empty string.
     *
     * The URL composition depends on the backend system being used for the conference.
     *
     * Special handling is applied to sessions with a [room name][Session.room] which is part of
     * the collection of [special room names][specialRoomNames]. If there an URL defined then
     * it is returned. If there is no URL defined then no composition is tried but instead
     * an empty string is returned.
     */
    fun getSessionUrl() = session.sessionUrl

    private val Session.sessionUrl: String
        get() = when (serverBackEndType) {
            PENTABARF.name -> getComposedSessionUrl(session.slug)
            else -> if (url.isNullOrEmpty()) {
                if (specialRoomNames.contains(room)) {
                    NO_URL
                } else {
                    getComposedSessionUrl(sessionId)
                }
            } else {
                url
            }
        }

    private fun getComposedSessionUrl(sessionIdentifier: String) =
            String.format(sessionUrlTemplate, sessionIdentifier)

    private companion object {
        const val NO_URL = ""
    }

}
