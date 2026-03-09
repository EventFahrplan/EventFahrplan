package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

internal class AlarmsViewModelFactory(
    private val context: Context,
) : Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appRepository = AppRepository
        @Suppress("UNCHECKED_CAST")
        return AlarmsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            alarmServices = AlarmServices.newInstance(context, appRepository),
            alarmsStateFactory = AlarmsStateFactory(
                resourceResolving = ResourceResolver(context),
                formattingDelegate = DateFormatterDelegate,
            ),
        ) as T
    }

}
