package nerd.tuxmobil.fahrplan.congress.search.languages

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.LANGUAGE_DELIMITER
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.LanguageTextProvider

class SessionLanguageDisplayNameFormatter(
    private val resourceResolving: ResourceResolving,
    private val languageTextProvider: LanguageTextProvider = LanguageTextProvider(resourceResolving),
) {

    fun format(rawValue: String): String {
        if (rawValue == SYNTHETIC_LANGUAGE_KEY_OTHER) {
            return resourceResolving.getString(R.string.search_filter_language_name_other)
        }
        return rawValue
            .split(LANGUAGE_DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(", ") { formatToken(it) }
    }

    private fun formatToken(token: String): String {
        val languageCode = token.removeSuffix("-formal")
        return languageTextProvider.getSearchLanguageDisplayName(languageCode)
    }

}
