package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class ChangeListViewModelFactory(
    private val context: Context,
) : Factory {

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
        val sessionChangeParametersFactory = SessionChangeParametersFactory(
            resourceResolving = resourceResolving,
            sessionPropertiesFormatting = sessionPropertiesFormatting,
            contentDescriptionFormatting = contentDescriptionFormatting,
            daySeparatorFactory = daySeparatorFactory,
            formattingDelegate = formattingDelegate,
        )
        @Suppress("UNCHECKED_CAST")
        return ChangeListViewModel(
            repository = AppRepository,
            executionContext = AppExecutionContext,
            sessionChangeParametersFactory = sessionChangeParametersFactory,
        ) as T
    }
}
