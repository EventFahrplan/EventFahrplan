package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class ChangeListViewModelFactory(
    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val screenNavigation: ScreenNavigation,
    private val sessionPropertiesFormatter: SessionPropertiesFormatter,
    private val contentDescriptionFormatter: ContentDescriptionFormatter,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChangeListViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            screenNavigation = screenNavigation,
            sessionChangeParametersFactory = SessionChangeParametersFactory(
                resourceResolving = resourceResolving,
                sessionPropertiesFormatter = sessionPropertiesFormatter,
                contentDescriptionFormatter = contentDescriptionFormatter,
                onDateFormatter = { useDeviceTimeZone -> DateFormatter.newInstance(useDeviceTimeZone) }
            )
        ) as T
    }
}
