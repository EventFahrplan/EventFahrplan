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

    fun getEventUrl(): String {
        return event.eventUrl
    }

    private val Event.eventUrl: String get() {
            when (serverBackEndType) {
                    PENTABARF.name -> return getComposedEventUrl(event.slug)
                    else -> if (url == null || url.isEmpty()) return getComposedEventUrl(lecture_id) else return url
            }
    }

    private fun getComposedEventUrl(eventIdentifier: String) =
            String.format(eventUrlTemplate, eventIdentifier)

}
