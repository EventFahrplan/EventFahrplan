package nerd.tuxmobil.fahrplan.congress.search

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE
import nerd.tuxmobil.fahrplan.congress.search.TenseType.PAST

class TenseTypeProvider(val now: Moment) : TenseTypeProvision {

    override fun getTenseType(session: Session) = when (session.endsAt.isBefore(now)) {
        true -> PAST
        false -> FUTURE
    }

}
