package nerd.tuxmobil.fahrplan.congress.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvider
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext

internal class AboutViewModelFactory(
    private val resourceResolving: ResourceResolving,
    private val externalNavigation: ExternalNavigation,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AboutViewModel(
            executionContext = AppExecutionContext,
            externalNavigation = externalNavigation,
            aboutParameterFactory = AboutParameterFactory(BuildConfigProvider(), resourceResolving)
        ) as T
    }

}
