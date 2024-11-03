package nerd.tuxmobil.fahrplan.congress.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class SearchViewModelFactory(
    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatter: SessionPropertiesFormatter,
    private val contentDescriptionFormatter: ContentDescriptionFormatter,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(
            repository = appRepository,
            searchQueryFilter = SearchQueryFilter(),
            searchHistoryManager = SearchHistoryManager(appRepository),
            searchResultParameterFactory = SearchResultParameterFactory(
                resourceResolving = resourceResolving,
                sessionPropertiesFormatter = sessionPropertiesFormatter,
                contentDescriptionFormatter = contentDescriptionFormatter,
                formattingDelegate = DateFormatterDelegate,
            ),
        ) as T
    }
}
