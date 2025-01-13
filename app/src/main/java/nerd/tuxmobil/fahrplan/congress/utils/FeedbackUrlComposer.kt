package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromPretalx
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromWiki
import nerd.tuxmobil.fahrplan.congress.models.Session

class FeedbackUrlComposer(

        private val frabScheduleFeedbackUrlFormatString: String = BuildConfig.SCHEDULE_FEEDBACK_URL

) {

    /**
     * Returns the feedback URL for the [session] if it is available or can be composed otherwise
     * an empty string.
     *
     * The dedicated [Session.feedbackUrl] provides the feedback URL of the Frab or Pretalx instance
     * independent from the domain where the schedule.xml is hosted.
     *
     * The [Frab schedule feedback URL][getFrabScheduleFeedbackUrl] is composed from the session id.
     * For sessions extracted from the wiki of the Chaos Communication Congress aka. "self organized
     * sessions" an empty string is returned because there is no feedback system for them.
     */
    fun getFeedbackUrl(session: Session): String {
        if (session.originatesFromWiki) {
            return NO_URL
        }
        val feedbackUrl = session.feedbackUrl
        if (feedbackUrl != null) {
            return feedbackUrl.ifEmpty { NO_URL }
        }
        return if (session.originatesFromPretalx) {
            session.pretalxScheduleFeedbackUrl
        } else {
            getFrabScheduleFeedbackUrl(session.guid, frabScheduleFeedbackUrlFormatString)
        }
    }

    private fun getFrabScheduleFeedbackUrl(guid: String, frabScheduleFeedbackUrlFormatString: String): String {
        return if (frabScheduleFeedbackUrlFormatString.isEmpty()) {
            NO_URL
        } else {
            String.format(frabScheduleFeedbackUrlFormatString, guid)
        }
    }

    private val Session.pretalxScheduleFeedbackUrl
        get() = "$url$PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX"

    private companion object {
        const val NO_URL = ""
        const val PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX = "feedback/"
    }

}
