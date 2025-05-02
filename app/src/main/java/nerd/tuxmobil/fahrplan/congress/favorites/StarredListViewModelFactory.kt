package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameterFactory
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting

class StarredListViewModelFactory(
    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        val formattingDelegate = DateFormatterDelegate
        @Suppress("UNCHECKED_CAST")
        return StarredListViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            logging = logging,
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            searchResultParameterFactory = SearchResultParameterFactory(
                resourceResolving = resourceResolving,
                sessionPropertiesFormatting = sessionPropertiesFormatting,
                contentDescriptionFormatting = contentDescriptionFormatting,
                DaySeparatorFactory(
                    resourceResolving = resourceResolving,
                    formattingDelegate = formattingDelegate,
                    contentDescriptionFormatting = contentDescriptionFormatting,
                ),
                formattingDelegate = formattingDelegate,
            ),
        ) as T
    }

}
