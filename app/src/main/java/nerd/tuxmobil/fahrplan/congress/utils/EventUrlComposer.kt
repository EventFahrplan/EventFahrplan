package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PENTABARF
import nerd.tuxmobil.fahrplan.congress.models.Session as Event

class EventUrlComposer @JvmOverloads constructor(

        private val event: Event,
        private val eventUrlTemplate: String = BuildConfig.EVENT_URL,
        private val serverBackEndType: String = BuildConfig.SERVER_BACKEND_TYPE,
        private val specialRoomNames: Set<String> = setOf(AppRepository.ENGELSYSTEM_ROOM_NAME)

) {

    /**
     * Returns the website URL for the [event] if it can be composed
     * otherwise an empty string.
     *
     * The URL composition depends on the backend system being used for the conference.
     *
     * Special handling is applied to events with a [room name][Event.room] which is part of
     * the collection of [special room names][specialRoomNames]. If there an URL defined then
     * it is returned. If there is no URL defined then no composition is tried but instead
     * an empty string is returned.
     */
    fun getEventUrl() = event.eventUrl

    private val Event.eventUrl: String
        get() = when (serverBackEndType) {
            PENTABARF.name -> getComposedEventUrl(event.slug)
            else -> if (url.isNullOrEmpty()) {
                if (specialRoomNames.contains(room)) {
                    NO_URL
                } else {
                    getComposedEventUrl(lectureId)
                }
            } else {
                url
            }
        }

    private fun getComposedEventUrl(eventIdentifier: String) =
            String.format(eventUrlTemplate, eventIdentifier)

    companion object {
        private const val NO_URL = ""
    }

}
