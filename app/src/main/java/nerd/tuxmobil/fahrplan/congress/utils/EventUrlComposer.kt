package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PENTABARF
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class EventUrlComposer @JvmOverloads constructor(

        private val event: Event,
        private val eventUrlTemplate: String = BuildConfig.EVENT_URL,
        private val serverBackEndType: String = BuildConfig.SERVER_BACKEND_TYPE

) {

    fun getEventUrl() = event.eventUrl

    private val Event.eventUrl: String
        get() = when (serverBackEndType) {
            PENTABARF.name -> getComposedEventUrl(event.slug)
            else -> if (url.isNullOrEmpty()) getComposedEventUrl(lectureId) else url
        }

    private fun getComposedEventUrl(eventIdentifier: String) =
            String.format(eventUrlTemplate, eventIdentifier)

}
