package nerd.tuxmobil.fahrplan.congress.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvider
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigator
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext

internal class AboutViewModelFactory(
    private val context: Context,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val resourceResolving = ResourceResolver(context)
        val externalNavigation = ExternalNavigator(context)
        @Suppress("UNCHECKED_CAST")
        return AboutViewModel(
            executionContext = AppExecutionContext,
            externalNavigation = externalNavigation,
            aboutParameterFactory = AboutParameterFactory(BuildConfigProvider(), resourceResolving)
        ) as T
    }

}
