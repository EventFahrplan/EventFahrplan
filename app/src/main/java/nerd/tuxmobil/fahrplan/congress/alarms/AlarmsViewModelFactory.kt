package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

internal class AlarmsViewModelFactory(
    private val appRepository: AppRepository,
    private val resourceResolving: ResourceResolving,
    private val alarmServices: AlarmServices,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlarmsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            alarmServices = alarmServices,
            alarmsStateFactory = AlarmsStateFactory(resourceResolving, DateFormatterDelegate),
        ) as T
    }

}
