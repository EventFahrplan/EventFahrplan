package nerd.tuxmobil.fahrplan.congress.search

import androidx.annotation.StringRes
import nerd.tuxmobil.fahrplan.congress.models.Session

interface SearchFilter {
    @get:StringRes
    val label: Int

    fun isMatch(session: Session, query: String): Boolean
}
