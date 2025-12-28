package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.models.Session

interface TenseTypeProvision {
    fun getTenseType(session: Session): TenseType
}
