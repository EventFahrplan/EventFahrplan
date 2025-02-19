package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session

fun interface FeedbackUrlComposition {
    fun getFeedbackUrl(session: Session): String
}
