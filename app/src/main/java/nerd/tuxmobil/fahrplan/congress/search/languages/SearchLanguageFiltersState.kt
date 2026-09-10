package nerd.tuxmobil.fahrplan.congress.search.languages

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class SearchLanguageFiltersState {

    private val mutableSelectedLanguageKeys = MutableStateFlow<Set<String>>(emptySet())
    val selectedLanguageKeys = mutableSelectedLanguageKeys.asStateFlow()

    fun toggle(key: String) {
        mutableSelectedLanguageKeys.update { current ->
            if (key in current) current - key else current + key
        }
    }

    fun unselect(key: String): Boolean {
        if (key !in mutableSelectedLanguageKeys.value) {
            return false
        }
        mutableSelectedLanguageKeys.update { it - key }
        return true
    }

    fun retainKeys(keys: Set<String>) {
        mutableSelectedLanguageKeys.update { it.intersect(keys) }
    }

    fun augmentActiveFilters(filters: Set<SearchFilter>, selectedLanguages: Set<String>) =
        when (selectedLanguages.isEmpty()) {
            true -> filters
            false -> filters + LanguagesSearchFilter(selectedLanguages)
        }

}
