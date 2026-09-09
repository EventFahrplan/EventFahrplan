package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

class LanguageTextProvider(private val resourceResolving: ResourceResolving) {

    fun getSearchLanguageDisplayName(languageCode: String) =
        if (languageCode.isEmpty()) {
            resourceResolving.getString(R.string.search_filter_language_name_other)
        } else
            getLanguageName(languageCode)

    fun getLanguageContentDescription(languageCode: String) =
        if (languageCode.isEmpty()) {
            resourceResolving.getString(R.string.session_list_item_language_unknown_content_description)
        } else {
            resourceResolving.getString(
                R.string.session_list_item_language_content_description,
                getLanguageName(languageCode),
            )
        }

    private fun getLanguageName(languageCode: String) = when (languageCode) {
        "en" -> resourceResolving.getString(R.string.session_list_item_language_english_content_description)
        "de" -> resourceResolving.getString(R.string.session_list_item_language_german_content_description)
        "pt" -> resourceResolving.getString(R.string.session_list_item_language_portuguese_content_description)
        else -> languageCode
    }

}
