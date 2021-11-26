package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromPretalx
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromWiki
import nerd.tuxmobil.fahrplan.congress.models.Session

class FeedbackUrlComposer(

        private val frabScheduleFeedbackUrlFormatString: String = BuildConfig.SCHEDULE_FEEDBACK_URL

) {

    /**
     * Returns the feedback URL for the [session] if it can be composed
     * otherwise an empty string.
     *
     * The [Frab schedule feedback URL][getFrabScheduleFeedbackUrl] is
     * composed from the session id.
     * For sessions extracted from the wiki of the Chaos Communication Congress
     * aka. "self organized sessions" an empty string is returned because
     * there is no feedback system for them.
     */
    fun getFeedbackUrl(session: Session): String {
        if (session.originatesFromWiki) {
            return NO_URL
        }
        return if (session.originatesFromPretalx) {
            session.pretalxScheduleFeedbackUrl
        } else {
            getFrabScheduleFeedbackUrl(session.sessionId, frabScheduleFeedbackUrlFormatString)
        }
    }

    private fun getFrabScheduleFeedbackUrl(sessionId: String, frabScheduleFeedbackUrlFormatString: String): String {
        return if (frabScheduleFeedbackUrlFormatString.isEmpty()) {
            NO_URL
        } else {
            String.format(frabScheduleFeedbackUrlFormatString, sessionId)
        }
    }

    private val Session.pretalxScheduleFeedbackUrl
        get() = "$url$PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX"

    private companion object {
        const val NO_URL = ""
        const val PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX = "feedback/"
    }

}
