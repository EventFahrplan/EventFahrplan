package nerd.tuxmobil.fahrplan.congress.search.filters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

/**
 * Matches sessions which have not ended yet, i.e. upcoming and currently running sessions.
 * Consistent with [nerd.tuxmobil.fahrplan.congress.search.TenseTypeProvider] which renders
 * all other sessions as past sessions.
 *
 * The current moment is obtained via [nowProvider] on each match to stay up-to-date.
 */
class UpcomingSearchFilter(
    private val nowProvider: () -> Moment = { Moment.now() },
) : SearchFilter {
    override val label = R.string.search_filter_upcoming

    override fun isMatch(session: Session, query: String): Boolean {
        return !session.endsAt.isBefore(nowProvider())
    }
}
