package nerd.tuxmobil.fahrplan.congress.search.languages

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

internal object FakeLanguageResourceResolver : ResourceResolving {

    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.session_list_item_language_english_content_description -> "English"
        R.string.session_list_item_language_german_content_description -> "German"
        R.string.session_list_item_language_portuguese_content_description -> "Portuguese"
        R.string.search_filter_language_name_other -> "Other"
        else -> error("Unexpected string resource id: $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any) = "irrelevant"

}
