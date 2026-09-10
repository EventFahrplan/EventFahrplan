package nerd.tuxmobil.fahrplan.congress.search.languages

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

data class SearchLanguageFilterUiState(
    val languageOptions: ImmutableList<LanguageOption>,
) {

    class Factory(resourceResolving: ResourceResolving) {

        val formatter = SessionLanguageDisplayNameFormatter(resourceResolving)

        fun of(languageKeys: List<String>, selectedLanguages: Set<String>): SearchLanguageFilterUiState? {
            if (languageKeys.isEmpty()) {
                return null
            }

            val languageOptions = languageKeys.map { key ->
                LanguageOption(
                    filterKey = key,
                    displayName = formatter.format(key),
                    selected = key in selectedLanguages,
                )
            }
            return SearchLanguageFilterUiState(languageOptions.toImmutableList())
        }
    }

    val selectedCount: Int
        get() = languageOptions.count { it.selected }

    /**
     * Filtering by language is pointless when all sessions share
     * the same single language entry, including "Other".
     */
    val enabled: Boolean
        get() = languageOptions.size > 1

}
