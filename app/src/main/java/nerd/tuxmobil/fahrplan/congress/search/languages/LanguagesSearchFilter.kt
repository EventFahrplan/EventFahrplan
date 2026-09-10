package nerd.tuxmobil.fahrplan.congress.search.languages

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.normalizedLanguage
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

internal class LanguagesSearchFilter(
    private val selectedLanguages: Set<String>,
) : SearchFilter {

    override val label = R.string.search_filter_language

    override fun isMatch(session: Session, query: String) = when (session.language.isEmpty()) {
        true -> SYNTHETIC_LANGUAGE_KEY_OTHER in selectedLanguages
        false -> session.normalizedLanguage in selectedLanguages
    }

}
