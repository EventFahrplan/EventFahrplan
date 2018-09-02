package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig.EVENT_URL
import nerd.tuxmobil.fahrplan.congress.BuildConfig.SERVER_BACKEND_TYPE
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.*
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class EventUrlComposer(private val event: Event) {

    fun getEventUrl(): String = when (SERVER_BACKEND_TYPE) {
        PENTABARF.name -> getComposedEventUrl(event.slug)
        FRAB.name -> getComposedEventUrl(event.lecture_id)
        PRETALX.name -> event.url
        else -> throw NotImplementedError("Unknown server backend type: '$SERVER_BACKEND_TYPE'")
    }

    private fun getComposedEventUrl(eventIdentifier: String) =
            String.format(EVENT_URL, eventIdentifier)

}
