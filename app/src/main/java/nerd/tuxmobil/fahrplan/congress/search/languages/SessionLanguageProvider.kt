package nerd.tuxmobil.fahrplan.congress.search.languages

import nerd.tuxmobil.fahrplan.congress.extensions.normalizedLanguage
import nerd.tuxmobil.fahrplan.congress.models.Session

fun List<Session>.getDistinctLanguageKeys(): List<String> {
    if (isEmpty()) {
        return emptyList()
    }
    val hasEmptyLanguage = any { it.language.isEmpty() }
    val nonEmptyLanguages = map { it.normalizedLanguage }
        .filter { it.isNotEmpty() }
        .distinct()
        .sorted()
    return if (hasEmptyLanguage) {
        nonEmptyLanguages + SYNTHETIC_LANGUAGE_KEY_OTHER
    } else {
        nonEmptyLanguages
    }
}
