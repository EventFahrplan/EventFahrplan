package nerd.tuxmobil.fahrplan.congress.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class SearchViewModelFactory(
    private val context: Context,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val resourceResolving = ResourceResolver(context)
        val appRepository = AppRepository
        val contentDescriptionFormatting = ContentDescriptionFormatter(resourceResolving)
        val formattingDelegate = DateFormatterDelegate

        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(
            repository = appRepository,
            searchQueryFilter = SearchQueryFilter(),
            searchHistoryManager = SearchHistoryManager(appRepository),
            searchResultParameterFactory = DefaultSearchResultParameterFactory(
                resourceResolving = resourceResolving,
                sessionPropertiesFormatting = SessionPropertiesFormatter(resourceResolving),
                contentDescriptionFormatting = contentDescriptionFormatting,
                daySeparatorFactory = DaySeparatorFactory(
                    resourceResolving = resourceResolving,
                    formattingDelegate = formattingDelegate,
                    contentDescriptionFormatting = contentDescriptionFormatting,
                ),
                formattingDelegate = formattingDelegate,
            ),
        ) as T
    }
}
