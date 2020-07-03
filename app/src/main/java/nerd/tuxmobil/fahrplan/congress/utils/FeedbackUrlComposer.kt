package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromPretalx
import nerd.tuxmobil.fahrplan.congress.extensions.originatesFromWiki
import nerd.tuxmobil.fahrplan.congress.models.Session

class FeedbackUrlComposer(

        private val session: Session,
        private val frabScheduleFeedbackUrlFormatString: String = BuildConfig.SCHEDULE_FEEDBACK_URL

) {

    /**
     * Returns the feedback URL for the [session] if it can be composed
     * otherwise an empty string.
     *
     * The [Frab schedule feedback URL][frabScheduleFeedbackUrl] is
     * composed from the session id.
     * For sessions extracted from the wiki of the Chaos Communication Congress
     * aka. "self organized sessions" an empty string is returned because
     * there is no feedback system for them.
     */
    fun getFeedbackUrl(): String {
        if (session.originatesFromWiki) {
            return NO_URL
        }
        return if (session.originatesFromPretalx) {
            session.pretalxScheduleFeedbackUrl
        } else {
            session.frabScheduleFeedbackUrl
        }
    }

    private val Session.frabScheduleFeedbackUrl
        get() =
            if (frabScheduleFeedbackUrlFormatString.isEmpty()) NO_URL
            else String.format(frabScheduleFeedbackUrlFormatString, sessionId)

    private val Session.pretalxScheduleFeedbackUrl
        get() = "$url$PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX"

    companion object {
        private const val NO_URL = ""
        private const val PRETALX_SCHEDULE_FEEDBACK_URL_SUFFIX = "feedback/"
    }

}
