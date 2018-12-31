package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromPretalx
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.*
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class EventUrlComposer @JvmOverloads constructor(

        private val event: Event,
        private val eventUrlTemplate: String = BuildConfig.EVENT_URL,
        private val serverBackEndType: String = BuildConfig.SERVER_BACKEND_TYPE

) {

    fun getEventUrl(): String = when (serverBackEndType) {
        PENTABARF.name -> getComposedEventUrl(event.slug)
        FRAB.name -> event.eventUrl
        PRETALX.name -> event.url
        else -> throw NotImplementedError("Unknown server backend type: '$serverBackEndType'")
    }

    private val Event.eventUrl
        get() = if (originatesFromPretalx) url else getComposedEventUrl(lecture_id)

    private fun getComposedEventUrl(eventIdentifier: String) =
            String.format(eventUrlTemplate, eventIdentifier)

}
