package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.DefaultSearchResultParameterFactory
import nerd.tuxmobil.fahrplan.congress.search.TenseTypeProvider
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class StarredListViewModelFactory(
    val context: Context,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val resourceResolving = ResourceResolver(context)
        val sessionPropertiesFormatting = SessionPropertiesFormatter(resourceResolving)
        val contentDescriptionFormatting = ContentDescriptionFormatter(resourceResolving)
        val formattingDelegate = DateFormatterDelegate
        val daySeparatorFactory = DaySeparatorFactory(
            resourceResolving = resourceResolving,
            formattingDelegate = formattingDelegate,
            contentDescriptionFormatting = contentDescriptionFormatting,
        )
        val searchResultParameterFactory = DefaultSearchResultParameterFactory(
            resourceResolving = resourceResolving,
            sessionPropertiesFormatting = sessionPropertiesFormatting,
            contentDescriptionFormatting = contentDescriptionFormatting,
            daySeparatorFactory = daySeparatorFactory,
            formattingDelegate = formattingDelegate,
            tenseTypeProvision = TenseTypeProvider(Moment.now()),
        )
        @Suppress("UNCHECKED_CAST")
        return StarredListViewModel(
            repository = AppRepository,
            executionContext = AppExecutionContext,
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            searchResultParameterFactory = searchResultParameterFactory,
        ) as T
    }

}
