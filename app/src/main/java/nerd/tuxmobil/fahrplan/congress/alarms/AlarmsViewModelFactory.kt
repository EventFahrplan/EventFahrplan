package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

internal class AlarmsViewModelFactory(

    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val alarmServices: AlarmServices,
    private val screenNavigation: ScreenNavigation,

    ) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlarmsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            resourceResolving = resourceResolving,
            alarmServices = alarmServices,
            screenNavigation = screenNavigation,
        ) as T
    }

}
