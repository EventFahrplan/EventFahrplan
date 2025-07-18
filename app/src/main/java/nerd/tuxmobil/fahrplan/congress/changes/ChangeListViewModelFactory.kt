package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorFactory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting

class ChangeListViewModelFactory(
    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting,
    private val contentDescriptionFormatting: ContentDescriptionFormatting,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val formattingDelegate = DateFormatterDelegate
        @Suppress("UNCHECKED_CAST")
        return ChangeListViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            sessionChangeParametersFactory = SessionChangeParametersFactory(
                resourceResolving = resourceResolving,
                sessionPropertiesFormatting = sessionPropertiesFormatting,
                contentDescriptionFormatting = contentDescriptionFormatting,
                daySeparatorFactory = DaySeparatorFactory(
                    resourceResolving = resourceResolving,
                    formattingDelegate = formattingDelegate,
                    contentDescriptionFormatting = contentDescriptionFormatting,
                ),
                formattingDelegate = formattingDelegate,
            )
        ) as T
    }
}
