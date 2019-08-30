package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromPretalx
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class FeedbackUrlComposer(

        private val event: Event,
        private val frabScheduleFeedbackUrlFormatString: String = BuildConfig.SCHEDULE_FEEDBACK_URL

) {

    /**
     * Returns the feedback URL for the [event] if it can be composed
     * otherwise an empty string.
     *
     * The [Frab schedule feedback URL][frabScheduleFeedbackUrl] is
     * composed from the event id. Currently, events extracted from
     * the wiki of the Chaos Communication Congress cannot be
     * distinguished from regular Frab events. Therefore, visiting the
     * resulting schedule feedback URL results in an HTTP 404 error.
     */
    fun getFeedbackUrl() = if (event.originatesFromPretalx) {
        event.pretalxScheduleFeedbackUrl
    } else {
        event.frabScheduleFeedbackUrl
    }

    private val Event.frabScheduleFeedbackUrl
        get() =
            if (frabScheduleFeedbackUrlFormatString.isEmpty()) NO_URL
            else String.format(frabScheduleFeedbackUrlFormatString, lectureId)

    private val Event.pretalxScheduleFeedbackUrl
        get() = "$url$PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX"

    companion object {
        private const val NO_URL = ""
        private const val PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX = "/feedback/"
    }

}
